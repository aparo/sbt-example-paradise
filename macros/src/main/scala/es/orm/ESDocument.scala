package es.orm

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Set

object esMacro {

  object DefType extends Enumeration {
    type DefType = Value
    val CLASSDEF = Value
    val DEFDEF = Value
    val IMPORTDEF = Value
    val ENUMDEF = Value
    val EMBEDDEF = Value
    val OTHER = Value
  }

  import DefType._

  object ClassFlag extends Enumeration {
    type ClassFlag = Value
    val PARTDEF = Value
    val ENTITYDEF = Value
    val TIMESTAMPSDEF = Value
    val OTHER = Value
  }

  import ClassFlag._

  object FieldFlag extends Enumeration {
    type FieldFlag = Value
    val OPTION = Value
    val CASE = Value
    val PART = Value
    val LIST = Value
    val INDEX = Value
    val PK = Value
    val UNIQUE = Value
    val DBTYPE = Value
  }

  import FieldFlag._

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    import Flag._

    val reservedNames = List("id", "dateCreated", "lastUpdated")
    val CASEACCESSOR = (1 << 24).toLong.asInstanceOf[FlagSet]
    val PARAMACCESSOR = (1 << 29).toLong.asInstanceOf[FlagSet]

    val caseAccessor = scala.reflect.internal.Flags.CASEACCESSOR.asInstanceOf[Long].asInstanceOf[FlagSet]

    val paramAccessor = scala.reflect.internal.Flags.PARAMACCESSOR.asInstanceOf[Long].asInstanceOf[FlagSet]
    val paramDefault = scala.reflect.internal.Flags.DEFAULTPARAM.asInstanceOf[Long].asInstanceOf[FlagSet]
    val param = scala.reflect.internal.Flags.PARAM.asInstanceOf[Long].asInstanceOf[FlagSet]
    val mutable = scala.reflect.internal.Flags.MUTABLE.asInstanceOf[Long].asInstanceOf[FlagSet]
    val optionalDate = Select(Select(Ident(newTermName("java")), newTermName("sql")), newTypeName("Timestamp"))

    def dateVal(name: String) = ValDef(Modifiers(mutable | caseAccessor | paramAccessor), newTermName(name), optionalDate, EmptyTree)
    def dateValInCtor(name: String) = ValDef(Modifiers(param | paramAccessor | paramDefault), newTermName(name), optionalDate, Literal(Constant(null))) // Ident(newTermName("None")))

    class ModDesc(var mods: Modifiers, name: TermName, var parents: List[Tree], self: ValDef, body: List[Tree], var esArguments:Map[String, Any]){
      val extraMethods: ListBuffer[Tree]=ListBuffer()

      def fixParents(){
        if (!parents.exists(t => checkParent(t, "ESMeta"))){
          parents = AppliedTypeTree(Select(Select(Ident(newTermName("es")), newTermName("orm")), newTypeName("ESMeta")), List(Ident(name))) :: parents
        }
      }

      fixParents()

      //      addMethod(DefDef(NoMods, newTermName("esIndex"), List(), List(List()), TypeTree(), Literal(Constant(esArguments.getOrElse("index", "default")))))
      //      addMethod(DefDef(NoMods, newTermName("typeName"), List(), List(List()), TypeTree(), Literal(Constant(esArguments.getOrElse("dtype", name.decoded.toLowerCase())))))
      //      addMethod(DefDef(NoMods, newTermName("esBulkSize"), List(), List(List()), TypeTree(), Literal(Constant(esArguments.getOrElse("bulkSize", 100)))))

      def getTree:Tree =ModuleDef(mods, name, Template(parents, self, body ++ extraMethods))

      def addMethod(tree:Tree)={
        //        println(showRaw(tree))
        //        println(tree)
        extraMethods += tree
      }


    }

    object ModDesc{
      def apply(tree: Tree, esArguments:Map[String, Any]):ModDesc={
        tree match {
          case ModuleDef(mods, name, Template(parents, self, body)) =>
            new ModDesc(mods, name, parents, self, body, esArguments=esArguments)
        }
      }
    }

    class ClsDesc(val mods: Modifiers, val name: TypeName, types: List[TypeDef], var parents: List[Tree], self: ValDef, var body: List[Tree], esArguments:Map[String, Any]) {
      var flags: Set[ClassFlag]=Set()
      val fields: ListBuffer[FldDesc]=ListBuffer()
      val objectName=newTermName(name.decoded)
      val extraMethods: ListBuffer[Tree]=ListBuffer()
      parseBody()

      def parseBody() {
        body.foreach {
          it =>
            it match {
              case ValDef(modifiers, _, _, _) if (modifiers.hasFlag(CASEACCESSOR) && modifiers.hasFlag(PARAMACCESSOR)) =>
                fields += FldDesc(it)
              case _ =>
            }

        }
      }


      //      private def fixParents(){
      //        if(!parents.exists(t => checkParent(t, "WithId"))){
      //          parents = Select(Select(Ident(newTermName("es")), newTermName("orm")), newTypeName("WithId")) :: parents
      //        }
      //
      //        if(!parents.exists(t => checkParent(t, "ToJson"))){
      //          parents = Select(Select(Ident(newTermName("es")), newTermName("orm")), newTypeName("ToJson")) :: parents
      //        }
      //
      //
      //        if(!parents.exists(t => checkParent(t, "Model"))){
      //          parents = Select(Select(Ident(newTermName("es")), newTermName("orm")), newTypeName("Model")) :: parents
      //        }
      //      }
      //
      //      fixParents()
      //      addMethod(q"""def save(bulk: Boolean = false) {
      //    $objectName.save(this, bulk = bulk)
      //  }""")

      def getTree():Tree =ClassDef(mods, name, types, Template(parents, self, body ++ extraMethods))

      def addMethod(tree:Tree)={
        extraMethods += tree
      }

      def part: Boolean = flags.exists(_ == PARTDEF)

      def entity: Boolean = flags.exists(_ == ENTITYDEF)

      def timestamps: Boolean = flags.exists(_ == TIMESTAMPSDEF)

      def dateVals: List[ValDef] = if (timestamps) dateVal("dateCreated") :: dateVal("lastUpdated") :: Nil else Nil

      def dateValsInCtor: List[ValDef] = if (timestamps) dateValInCtor("dateCreated") :: dateValInCtor("lastUpdated") :: Nil else Nil

      def dateDefs =
        if (timestamps)
          c.parse("""def dateCreated = column[java.sql.Timestamp]("dateCreated")""") :: c.parse("""def lastUpdated = column[java.sql.Timestamp]("lastUpdated")""") :: Nil
        else
          Nil

      def foreignKeys: List[FldDesc] = {
        fields.filter {
          it => it.flags.exists(_ == FieldFlag.CASE) && !it.flags.exists(_ == FieldFlag.LIST)
        }.toList
      }

      def assocs: List[FldDesc] = {
        fields.filter {
          it => it.flags.exists(_ == FieldFlag.CASE) && it.flags.exists(_ == FieldFlag.LIST)
        }.toList
      }

      def simpleValDefs: List[FldDesc] = {
        fields.filter {
          it => !it.flags.exists(_ == FieldFlag.LIST)
        }.toList
      }

      def listValDefs: List[FldDesc] = {
        fields.filter {
          it => it.flags.exists(_ == FieldFlag.LIST)
        }.toList
      }

      def allFields = {
        fields.toList.map {
          it =>
            if (it.part)
              it.cls.get.fields.toList
            else
              it :: Nil
        }.flatten
      }

      def indexes: List[FldDesc] = {
        allFields.filter {
          it =>
            it.flags.exists(_ == FieldFlag.INDEX)
        }.toList
      }
    }

    object ClsDesc {
      def apply(tree: Tree, esArguments:Map[String, Any]) = {
        val ClassDef(mods, name, types, Template(parents, self, body)) = tree
        if (!mods.hasFlag(CASE))
          c.abort(c.enclosingPosition, s"Only case classes allowed here ${name.decoded}")
        val annotations = mods.annotations.map(_.children.head.toString)
        val isPart = annotations.exists(_ == "new Part")
        val flags = Set.empty[ClassFlag]
        if (isPart)
          flags += PARTDEF
        else
          flags += ENTITYDEF
        val timestamps = mods.annotations.exists(_.toString.indexOf("timestamps = true") >= 0) // quick & dirty
        if (timestamps) flags += TIMESTAMPSDEF
        val result = new ClsDesc(mods, name, types, parents, self, body, esArguments=esArguments)
        result.flags=flags
        //name.decoded, flags, ListBuffer(), tree
        result
      }
    }

    class FldDesc(val name: String, val fullTypeName: String, val typeName: String, val typeTree: Tree, val flags: Set[FieldFlag],
                  val options: FldOptions, val cls: Option[ClsDesc], val tree: Tree, val default: Tree) {
      def unique: Boolean = flags.exists(_ == FieldFlag.UNIQUE)

      def part: Boolean = flags.exists(_ == FieldFlag.PART)

      def option: Boolean = flags.exists(_ == FieldFlag.OPTION)

      def cse: Boolean = flags.exists(_ == FieldFlag.CASE)

      def internalType: Tree = typeTree match {
        case AppliedTypeTree(Ident(option), tpe :: Nil) if option.decoded == "Option" =>
          tpe
        case AppliedTypeTree(Ident(list), tpe :: Nil) if list.decoded == "List" =>
          tpe

        case _ => typeTree
      }
    }

    object FldDesc {
      def apply(fieldTree: Tree) = {
        val ValDef(mod, name, tpt, rhs) = fieldTree
        if (reservedNames.exists(_ == name.decoded))
          c.abort(c.enclosingPosition, s"Column with name ${name.decoded} not allowed")
        else {
          var defaultValue: Tree = q"""None"""
          val flags = Set[FieldFlag]()
          val annotation = mod.annotations.headOption.map(_.children.head.toString)
          val isIndex = annotation.exists(_ == "new Index")
          if (isIndex) {
            flags += FieldFlag.INDEX
            mod.annotations.headOption.foreach {
              it =>
                if (it.children.length >= 2 && it.children(1).toString.equals("true")) flags += FieldFlag.UNIQUE
            }
          }
          val isdbType = annotation.exists(_ == "new Type")
          if (isdbType) {
            flags += FieldFlag.DBTYPE
            mod.annotations.headOption.foreach {
              it =>
            }
          }
          def buildTypeName(tree: Tree): String = {
            tree match {
              case Select(subtree, name) =>
                buildTypeName(subtree) + "." + name.decoded
              case AppliedTypeTree(subtree, args) =>
                buildTypeName(subtree) + "[" + args.map(it => buildTypeName(it)).mkString(",") + "]"
              case Ident(x) =>
                x.decoded
              case other => other.toString
            }
          }
          val fullTypeName: String = buildTypeName(tpt)
          val typeName = fullTypeName
          val clsDesc: Option[ClsDesc] = None

          var fldOptions = FldOptions()

          tpt match {
            case AppliedTypeTree(Ident(option), tpe :: Nil) if option.decoded == "Option" =>
              flags += FieldFlag.OPTION
              fldOptions = fldOptions.copy(required = false)
              rhs match {
                case EmptyTree => defaultValue = q"None"
                case _ => defaultValue = rhs
              }
            case AppliedTypeTree(Ident(list), tpe :: Nil) if list.decoded == "List" =>
              flags += FieldFlag.LIST
              fldOptions=fldOptions.copy(multiple = true)
              rhs match {
                case EmptyTree => defaultValue = q"Some(Nil)"
                case _ => defaultValue = rhs
              }

            case _ => None
          }

          //Collection metadata
          var metadata:Map[String, Any]=Map.empty[String, Any]

          mod.annotations.foreach {
            it =>
            //            println("field modifier:" + showRaw(it))
              it match {
                case Apply(Select(New(Ident(index)), _), List(Literal(Constant(unique)))) =>
                  if (index.decoded == "Index") flags += FieldFlag.INDEX
                  if (unique == true) flags += FieldFlag.UNIQUE
                case Apply(Select(New(Ident(typeName)), _), args) =>
                  typeName.decoded match {
                    case "Fk" =>
                      if(fldOptions.metadata.contains("Nested") || fldOptions.metadata.contains("Embedded"))
                        c.abort(c.enclosingPosition, s"only one entity allowed here of type: Fk, Nested or Embedded")
                      fldOptions = fldOptions.copy(foreignKey = true)
                      metadata += ("Fk" -> true)

                    case "Nested" =>
                      if(fldOptions.metadata.contains("Fk") || fldOptions.metadata.contains("Embedded"))
                        c.abort(c.enclosingPosition, s"only one entity allowed here of type: Fk, Nested or Embedded")
                      metadata += ("Nested" -> true)
                      fldOptions = fldOptions.copy(nested = true)
                    case "Embedded" =>
                      if(fldOptions.metadata.contains("Fk") || fldOptions.metadata.contains("Nested"))
                        c.abort(c.enclosingPosition, s"only one entity allowed here of type: Fk, Nested or Embedded")
                      metadata += ("Embedded" -> true)
                      fldOptions = fldOptions.copy(embedded = true)
                    case "NoIndex" =>
                      if(fldOptions.metadata.contains("Analyzed") || fldOptions.metadata.contains("NotAnalyzed"))
                        c.abort(c.enclosingPosition, s"only one entity allowed here of type: NoIndex, Analyzed or NotAnalyzed")
                      metadata += ("index" -> false)
                      fldOptions=fldOptions.copy(index = "no")
                    case "Analyzed" =>
                      if(fldOptions.metadata.contains("NoIndex") || fldOptions.metadata.contains("NotAnalyzed"))
                        c.abort(c.enclosingPosition, s"only one entity allowed here of type: NoIndex, Analyzed or NotAnalyzed")
                      fldOptions=fldOptions.copy(index = "analyzed")
                      metadata += ("analyzed" -> true)
                    case "NotAnalyzed" =>
                      if(fldOptions.metadata.contains("NoIndex") || fldOptions.metadata.contains("Analyzed"))
                        c.abort(c.enclosingPosition, s"only one entity allowed here of type: NoIndex, Analyzed or NotAnalyzed")
                      fldOptions=fldOptions.copy(index = "not_analyzed")
                      metadata += ("analyzed" -> false)
                    case "Stored" =>
                      if(fldOptions.metadata.contains("NoStored"))
                        c.abort(c.enclosingPosition, s"only one entity allowed here of type: NoStored or Stored")
                      fldOptions=fldOptions.copy(store = true)
                      metadata += ("store" -> false)
                    case "NoStored" =>
                      if(fldOptions.metadata.contains("Stored"))
                        c.abort(c.enclosingPosition, s"only one entity allowed here of type: NoStored or Stored")
                      fldOptions=fldOptions.copy(store = false)
                      metadata += ("store" -> true)
                    case _ =>

                  }
                  args.foreach {
                    case AssignOrNamedArg(Ident(name), Apply(Ident(_), values)) =>
                      val valuesList = values.map {
                        case Literal(Constant(value)) => value.toString
                      }
//                      if (name.decoded == "types")
//                        fldOptions = fldOptions.copy(types = valuesList ++ fldOptions.types)
                  }
                //                if (typeName.decoded == "ESType") flags += FieldFlag.ESTYPE
                //                colType = dbTypeValue.asInstanceOf[String]
              }
          }
          if(metadata.size>0)
            fldOptions = fldOptions.copy(metadata = metadata)
          new FldDesc(name.decoded, fullTypeName, typeName, tpt, flags, fldOptions, clsDesc, fieldTree, defaultValue)
        }
      }
    }

    def typeToTermTree(myType:String):Tree={
      if (myType.contains("[")){
        val operator = myType.split("\\[")(0)
        val remainer = myType.split("\\[")(1).stripSuffix("]")
        return AppliedTypeTree(
          Ident(newTypeName(operator)),
          remainer.split(",").map(a => typeToType(a.trim)).toList
        )


      }

      val tokens=myType.split("\\.")
      var tree:Tree = Ident(newTermName(tokens.head))
      tokens.tail.foreach{
        name =>
          tree=Select(tree, newTermName(name))
      }
      tree
    }

    def typeToType(myType:String):Tree={
      if (myType.contains("[")){
        val operator = myType.split("\\[")(0)
        val remainer = myType.split("\\[")(1).stripSuffix("]")
        return AppliedTypeTree(
          Ident(newTypeName(operator)),
          remainer.split(",").map(a => typeToType(a.trim)).toList
        )
      }
      val tokens=myType.split("\\.")
      if (tokens.length==1)
        return Ident(newTypeName(tokens.head))

      var tree:Tree = Ident(newTermName(tokens.head))
      tokens.tail.foreach{
        name =>
          if(name==tokens.last)
            tree=Select(tree, newTypeName(name))
          else
            tree=Select(tree, newTermName(name))
      }
      tree
    }

    def checkParent(tree:Tree, name:String):Boolean = tree match {
      case Ident(identName) => identName.decoded==name
      case Select(_, vname) => vname.decoded==name
      case AppliedTypeTree(tr, _) => checkParent(tr, name)
      case _ => false
    }

    class ClsModClass(cls:ClsDesc, mod:ModDesc, arguments: Map[String, Any]){
      def getTrees:List[Tree]=List(cls.getTree, mod.getTree)

      def inject={
        val typeName=cls.name


        //        mod.addMethod(q"""def fromJson(json:String):$typeName={
        //        implicit val formats = es.orm.ModelSerializers.formats
        //        import net.liftweb.json.native.Serialization.read
        //        read[$typeName](json)
        //        } """
        //        )

        val fields_code=cls.fields.map{
          field =>
            val mapApply = Select(reify(Map).tree, newTermName("apply"))
            val pairs = field.options.metadata.collect {
              case (k,v) =>
                val name = c.literal(k)
                val value = v match {
                  case x:String => c.literal(x)
                  case x:Int => c.literal(x)
                  case x:Long => c.literal(x)
                  case x:Boolean => c.literal(x)
                  case x:Float => c.literal(x)
                  case x:Double => c.literal(x)
                  case x:Byte => c.literal(x)
                  case _ => c.literal(v.toString)
                }
                reify(name.splice -> value.splice).tree
            }
            val myMap=if(pairs.size==0) reify(Map.empty[String, Any]).tree else Apply(mapApply, pairs.toList)
            if(field.options.embedded||field.options.nested){
              q"""es.orm.FieldDescription[${typeToType(field.fullTypeName.toString)}, ${typeToType(typeName.toString)}](${field.name},
        classOf[${typeToType(field.internalType.toString)}],  ${myMap}, ${field.default},
        ${field.options.foreignKey}, ${field.options.index}, ${field.options.store}, ${field.options.nested},${field.options.embedded},
        required=${field.options.required}, multiple=${field.options.multiple}, builder=${typeToTermTree(field.internalType.toString)}) """
            } else{
              q"""es.orm.FieldDescription[${typeToType(field.fullTypeName.toString)}, ${typeToType(typeName.toString)}](${field.name},
        classOf[${typeToType(field.internalType.toString)}],  ${myMap}, ${field.default},
        ${field.options.foreignKey}, ${field.options.index}, ${field.options.store}, ${field.options.nested},${field.options.embedded},
        required=${field.options.required}, multiple=${field.options.multiple}, builder=${typeToTermTree(typeName.toString)}) """

            }
        }
        val fields = q"""override def fields:List[es.orm.FieldDescription[_, _]]=List(..$fields_code)"""
        mod.addMethod( fields)

        //        mod.addMethod(q"""def esMapping:es.mapping.DocumentObjectField={
        //        import es.mapping._
        //        val doc=new DocumentObjectField()
        //        $fields_code
        //        doc
        //        }""")

      }
    }

    object ClsModClass{
      def apply(cls:Option[ClsDesc]=None, mod:Option[ModDesc]=None, arguments: Map[String, Any]):ClsModClass={
        var module=mod
        if(!module.isDefined){
          val className=newTermName(cls.get.name.decoded)

          module=Some(
            q"""object $className {}""" match {
              case ModuleDef(mods, name, Template(parents, self, body)) =>
                new ModDesc(mods, name, List(AppliedTypeTree(Select(Select(Ident(newTermName("es")), newTermName("orm")), newTypeName("ESMeta")), List(Ident(cls.get.name)))), self, body, esArguments = arguments)
            })
        }

        new ClsModClass(cls.get, module.get, arguments)
      }
    }

    //    annottees.map(_.tree).toList.foreach(a => println(showRaw(a)))
    //    println(arguments)

    /* COMMENTED OUT ALL THE MACRO CODE TO LEAVE ONLY A PASS_THROUGH
    var myClass:Option[ClsDesc]=None
    var myModule:Option[ModDesc]=None
    val arguments= extractArguments(c)
    annottees.map(_.tree).toList.map {
      it =>
        it match {
          case ModuleDef(mods, name, Template(parents, self, body)) =>
            myModule=Some(ModDesc(it, arguments))
          case ClassDef(mods, name, types, Template(parents, self, body)) =>
            myClass=Some(ClsDesc(it, arguments))
          //esObject=esObject.copy(className=newTermName(name.decoded), typeName=name, mod=Some(cookClass(mods, name, types, parents, self, body)))
        }
    }.toList.asInstanceOf[List[Tree]]

    val esObject=ClsModClass(myClass, myModule, arguments=arguments)
    esObject.inject
    //We print the generated code
    println("="*80)
    esObject.getTrees.foreach(println)
    println("-"*80)
    c.Expr[Any](Block(esObject.getTrees, Literal(Constant(()))))
    */
    val result = {
      annottees.map(_.tree).toList.map {
        case ModuleDef(mods, name, Template(parents, self, body)) =>
          ModuleDef(mods, name, Template(parents, self, body))
        case ClassDef(mods, name, types, Template(parents, self, body)) =>
          ClassDef(mods, name, types, Template(parents, self, body))
      }
    }.asInstanceOf[List[Tree]]

    c.Expr[Any](Block(result, Literal(Constant(()))))

  }


  private def extractArguments(c: Context): Map[String, Any] = {
    import c.universe._
    import Flag._
    var result: Map[String, Any] = Map.empty

    //println(showRaw(c.prefix.tree))
    c.prefix.tree.foreach {
      it =>
        it match {
          //case Apply(Select(_, _), List(Literal(Constant(value)))) =>
          case Apply(_, values) =>
            values.foreach {
              value =>
                value match {
                  case AssignOrNamedArg(Ident(ident), Literal(Constant(v))) =>
                    result += (ident.decoded -> v)
                }
            }
          //          case Apply(k,v) =>
          //            println("Apply")
          //          case Select(k,v) =>
          //            println("Select ($k $v)")
          case _ =>

        }
    }
    result
  }
}

/*
* The ESDocument is a macro annotation that a compile time read the case class signature and create in the case class
* module a list of fields with signature information that will be used in orm functionalities.
* Only using class macro annotation we are able to capture annotations on fields and mainly the default values of
* fields.
* */

class ESDocument extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro esMacro.impl
}


//trait ToJson {
//  def toJson: String = {
//    import net.liftweb.json._
//    import net.liftweb.json.Serialization
//    import net.liftweb.json.Serialization.{read, write}
//
//    implicit val formats = Serialization.formats(NoTypeHints)
//    write(this)
//  }
//
//}
