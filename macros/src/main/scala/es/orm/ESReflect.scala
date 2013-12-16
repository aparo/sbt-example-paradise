package es.orm

import language.experimental.macros
import scala.language.higherKinds

import scala.reflect.macros.Context

trait ESReflect[T] {
  def reflectedFields:List[FieldDescription[_, _]]=macro ESReflectImpl.getFields[T]
}

object ESReflectImpl {
  def getFields[T: c.WeakTypeTag](c: Context): c.Expr[List[FieldDescription[_, _]]] = {
    import c.universe._
    import Flag._
    import definitions._
    val tpe = weakTypeOf[T]
    val ctor =tpe.member(nme.CONSTRUCTOR).asMethod



    class FldDesc(val name: String, var fullTypeName: Type, val options: FldOptions,
                  var typeTree: Type, var default: Tree=q"""None""") {

//      println(s"field $name")
//      internalType.baseClasses.foreach(b => println(b.name.decoded))
      options.embedded=internalType.baseClasses.exists(_.name.decoded=="Product")

      private def detectOptions={
        typeTree match {
          case TypeRef(tpe, sym, types) if sym.name.decoded == "Option" =>
            options.required=false
            default=q"""Some(None)"""

          case TypeRef(tpe, sym, types) if sym.name.decoded == "List" =>
            options.multiple=true
            default=q"""Some(Nil)"""

          case _ =>
        }
      }
      detectOptions

      def getInternalType(typ:Type):Type=
        typ match {
        case TypeRef(tpe, sym, types) if sym.name.decoded == "Option" =>
          getInternalType(types(0))
        case TypeRef(tpe, sym, types) if sym.name.decoded == "List" =>
          getInternalType(types(0))
        case _ =>
          typ
      }

      lazy val internalType: Type={
//        println(showRaw(typeTree))
        getInternalType(typeTree)
      }
    }


    var fields:Map[String, FldDesc]=Map()

    ctor.paramss.head.foreach{p =>
      val name: String = p.name.decoded.trim()
      fields += (name -> new FldDesc(name, p.typeSignature, FldOptions(), p.typeSignature))
    }

    tpe.members.foreach{p =>
//      println(s"annotation: ${p.name.decoded} ${p.annotations}")
      if(!p.annotations.isEmpty){
        val field = fields.get(p.name.decoded.trim)
        field match {
          case None =>
//            println(s"missing ${p.name.decoded.trim}")
          case _=>
            p.annotations.foreach{a =>
//              println(s"a.scalaArgs ${showRaw(a.scalaArgs)}")
//              println(s"a.javaArgs ${a.javaArgs}")
//              println(s"a.tpe ${a.tpe}")
              a.tpe.toString match {
                case "es.orm.ESFk @scala.annotation.meta.field" =>
                  field.get.options.embedded=false
                  field.get.options.nested=false
                  field.get.options.foreignKey=true
                case "es.orm.ESIndex @scala.annotation.meta.field" =>
                  field.get.options.index="yes"
                case "es.orm.ESNoIndex @scala.annotation.meta.field" =>
                  field.get.options.index="no"
                case "es.orm.ESNested @scala.annotation.meta.field" =>
                  field.get.options.embedded=false
                  field.get.options.nested=true
                case "es.orm.ESStored @scala.annotation.meta.field" =>
                  field.get.options.store=true
                case "es.orm.ESNoStored @scala.annotation.meta.field" =>
                  field.get.options.store=false
                case "es.orm.ESAnalyzed @scala.annotation.meta.field" =>
                  field.get.options.index="analyzed"
                case "es.orm.ESNotAnalyzed @scala.annotation.meta.field" =>
                  field.get.options.index="not_analyzed"
                case "es.orm.ESEmbedded @scala.annotation.meta.field" =>
                  field.get.options.embedded=true
                  field.get.options.nested=false
                  field.get.options.foreignKey=false
              }
            }
        }


      }
    }

//    println("tp")
//    ctor.typeParams.foreach(p => println(s"${p.name.decoded} ${showRaw(p)}"))
//    println("ps")
//    tpe.member(nme.CONSTRUCTOR).asMethod.paramss.foreach(p => println(s"$p ${showRaw(p)}"))
    val c0: c.type = c
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

    val fields_code=fields.map{
      case (name, field) =>
//        println(field.internalType)
//        println(showRaw(q"""es.orm.FieldDescription[Option[es.fixures.Person], es.fixures.Person]("father", classOf[es.fixures.Person], List(), None, true, "analyzed", false, required = true, multiple = false, builder = es.fixures.Person)"""))
//        println(showRaw(q"def a(b:Map[String, es.fixute.Metadata])=b"))
//        println(showRaw(typeToTermTree(field.fullTypeTree.toString())))
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
        val myMap=Apply(mapApply, pairs.toList)
        if(field.options.embedded||field.options.nested){
          q"""es.orm.FieldDescription[${typeToType(field.fullTypeName.toString)}, ${typeToType(tpe.toString)}](${field.name},
        classOf[${typeToType(field.internalType.toString)}],  ${myMap}, ${field.default},
        ${field.options.foreignKey}, ${field.options.index}, ${field.options.store}, ${field.options.nested},${field.options.embedded},
        required=${field.options.required}, multiple=${field.options.multiple}, builder=${typeToTermTree(field.internalType.toString)}) """
        } else{
          q"""es.orm.FieldDescription[${typeToType(field.fullTypeName.toString)}, ${typeToType(tpe.toString)}](${field.name},
        classOf[${typeToType(field.internalType.toString)}],  ${myMap}, ${field.default},
        ${field.options.foreignKey}, ${field.options.index}, ${field.options.store}, ${field.options.nested},${field.options.embedded},
        required=${field.options.required}, multiple=${field.options.multiple}, builder=${typeToTermTree(tpe.toString)}) """

        }
    }

    c.Expr[List[FieldDescription[_, _]]](q"""List(..$fields_code)""")
//    c.Expr[List[FieldDescription[_, _]]](q"""List()""")
  }

}
