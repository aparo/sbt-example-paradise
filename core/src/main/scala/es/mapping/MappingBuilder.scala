package es.mapping

import java.util.Date
import es.orm.{WithId, ESObject, MappingMixin, FieldDescription}
import scala.Some
import com.thoughtworks.paranamer.{BytecodeReadingParanamer, CachingParanamer}
import java.lang.reflect.{ParameterizedType, Type}
import scala.reflect.runtime.{universe => ru}
import scala.Predef._
import scala.Some
import net.liftweb.json.JsonAST.JString

//import net.thenetplanet.common.joda.time.{DateTime => TNPDateTime}

import org.joda.time.DateTime

object MappingBuilder {

  private val pn = new CachingParanamer(new BytecodeReadingParanamer)

  //val defaultFieldDescription = FieldDescription[Int, Int]("_default", classOf[Int])

  val JString = classOf[String]
  val JBoolean = classOf[Boolean]
  val JInteger = classOf[Int]
  val JLong = classOf[Long]
  val JDate = classOf[Date]
  //  val JTNPDateTime = classOf[TNPDateTime]
  val JodaDateTime = classOf[DateTime]
  val JMap = classOf[Map[_, _]]

  private def getType[_](option: Option[_]): Class[_] = {
    if (option.isEmpty) classOf[Nothing] else (option.get.asInstanceOf[AnyRef]).getClass
  }

  private def getClassFields(name: String): List[FieldDescription[_, _]] = try {
    val companionClass = Class.forName(name + "$")
    val moduleField = companionClass.getField("MODULE$")
    moduleField.get(null).asInstanceOf[MappingMixin].fields
  } catch {
    case e: Throwable => List()
  }

  private def extractType(value: Class[_]): ObjectField = {
    val newDoc = new ObjectField()
    if (value.getInterfaces.find(_ == classOf[scala.Product]) != None) {

      for {
        ctor <- value.getDeclaredConstructors.headOption
        parameters = pn.lookupParameterNames(ctor)

      } {
        val types = ctor.getGenericParameterTypes.map {
          gt =>
            gt match {
              case x: ParameterizedType =>
                x.getActualTypeArguments()(0)
              case _ =>
                gt
            }
        }.toList
        createMapping(newDoc, parameters, types, getClassFields(value.getName))
      }
    }
    newDoc
  }

  def getType[T](clazz: Class[T]): ru.Type = {
    val runtimeMirror = ru.runtimeMirror(clazz.getClassLoader)
    runtimeMirror.classSymbol(clazz).toType
  }

  def mapFieldDescriptor(name: String, fields: Seq[FieldDescription[_, _]]): Option[FieldDescription[_, _]] = {
    fields.filter(_.name == name).map(v => return Some(v))
    None
  }

  private def unWrapDescription(field: Option[FieldDescription[_, _]]): (Option[String], Option[Boolean], Boolean) = {
    field match {
      case Some(f) =>
        (Some(f.index), Some(f.store), f.foreignKey)
      case _ =>
        (Some("yes"), Some(true), false)
    }
  }

  lazy val mapDefaultMapping: ObjectField = new ObjectField()

  def createMapping(doc: BaseObjectField, fields: Seq[String], types: Seq[Type], fieldDescriptions: Seq[FieldDescription[_, _]]) {

    fields.zip(types) foreach {
      case (name, ctype) =>
        val fieldDesc = mapFieldDescriptor(name, fieldDescriptions)
        val (index, store, foreignKey) = unWrapDescription(fieldDesc)
        ctype match {
          case BigInt => doc.add(name, IntegerField(index = index, store = store))
          case JInteger => doc.add(name, IntegerField(index = index, store = store))
          case JLong => doc.add(name, LongField(index = index, store = store))
          case JString => doc.add(name, StringField(index = index, store = store))
          case JBoolean => doc.add(name, BooleanField(index = index, store = store))
          case JDate => doc.add(name, DateField(index = index, store = store))
          case JodaDateTime => doc.add(name, DateField(index = index, store = store))
          case _ if ctype.getClass.getCanonicalName == "sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl" =>
            doc.add(name, mapDefaultMapping)
          case _ =>
            if (foreignKey)
              doc.add(name, StringField(index = Some("not_analyzed"), store = store))
            else
              doc.add(name, extractType(ctype.asInstanceOf[Class[_]]))
        }

    }
  }

  def dumpAsMap(obj: Any, fields: List[FieldDescription[_, _]]): Map[String, Any] = {
    var result = Map.empty[String, Any]
    obj.getClass.getDeclaredFields.foreach {
      f =>
        f.setAccessible(true)
        val name = f.getName
        for (fieldDesc <- getFieldByName(name, fields)) {
          val value = f.get(obj)
          value match {
            case None =>
            case Nil =>
              result += (name -> List())
            case Some(v) =>
              result += (name -> toValue(v, fieldDesc))
            case l :: tail =>
              val results = toValue(l, fieldDesc) :: tail.map(v => toValue(v, fieldDesc))
              result += (name -> results)
            case m:Map[_,_] => //case m:Map[String,_] =>
              result += (name -> m)
            case _ =>
              result += (name -> toValue(value, fieldDesc))
          }
        }
    }
    result

  }

  def toValue(obj: Any, fdesc: FieldDescription[_, _]): Any = {
    obj match {
      case x if x.isInstanceOf[Int] =>
        x
      case x if x.isInstanceOf[Long] =>
        x
      case x if x.isInstanceOf[String] =>
        x
      case x if x.isInstanceOf[Float] =>
        x
      case x if x.isInstanceOf[Double] =>
        x
      case x if x.isInstanceOf[Boolean] =>
        x
      case x if x.isInstanceOf[DateTime] =>
        x

      case _ =>
        if (fdesc.foreignKey) {
          val esObj = obj.asInstanceOf[ESObject[_]]
          esObj._id match {
            case None =>
              esObj.save()
              return esObj._id.get
            case _ =>
              return esObj._id.get
          }
        }

        val fields = getClassFields(obj.getClass.getCanonicalName)

        dumpAsMap(obj, fields)
    }
  }

  def getFieldByName(name: String, fields: List[FieldDescription[_, _]]): Option[FieldDescription[_, _]] = {
    fields.filter(_.name == name).map(v => return Some(v))
    None
  }


  //
  //  def fromFields(fields:List[FieldDescription[_]]):DocumentObjectField={
  //    val doc = new DocumentObjectField()
  //    for(field<-fields;
  //      esField <- mapFieldDescription(field.name, field)
  //    ){
  //      doc.add(esField._1, esField._2)
  //    }
  //    doc
  //  }
  /*
    private def extractType(value: Class[_]): Option[(String,Field)] = {
      val newDoc = new ObjectField()
      if (value.getInterfaces.find(_ == classOf[scala.Product]) != None) {

        for {
          ctor <- value.getDeclaredConstructors.headOption
          parameters = pn.lookupParameterNames(ctor)

        } {
          val types = ctor.getGenericParameterTypes.map {
            gt =>
              gt match {
                case x: ParameterizedType =>
                  x.getActualTypeArguments()(0)
                case _ =>
                  gt
              }
          }.toList
          parameters.zip(types).foreach{
            case (p,typ)=>
              for{ m <- mapClass(p, typ.getClass,FieldDescription[Any](p, typ.getClass))}{
                newDoc.add(m._1, m._2)
              }


          }

          //createMapping(newDoc, parameters, types, getClassSuggested(value.getName))
        }
      }
      Some((value.getName, newDoc))
    }
  */
  //  def getType[T](clazz: Class[T]): ru.Type = {
  //    val runtimeMirror = ru.runtimeMirror(clazz.getClassLoader)
  //    runtimeMirror.classSymbol(clazz).toType
  //  }
  //  def mapFieldDescription(name:String, field:FieldDescription[_]):Option[(String,Field)]=mapClass(name, field.clazz, field)
  //
  //  def mapClass(name:String, clazz:Class[_], field:FieldDescription[_]):Option[(String,Field)]={
  //    clazz match {
  //      case JInteger => Some((name, IntegerField(index=Some(field.index), store=Some(field.store))))
  //      case JLong =>  Some((name, LongField(index=Some(field.index), store=Some(field.store))))
  //      case JString => Some((name, StringField(index=Some(field.index), store=Some(field.store))))
  //      case JBoolean => Some((name, BooleanField(index=Some(field.index), store=Some(field.store))))
  //      case JDate => Some((name, DateField(index=Some(field.index), store=Some(field.store))))
  //      //      case JTNPDateTime => DateField(index=Some(field.index), store=Some(field.store)))
  //      case JodaDateTime => Some((name, DateField(index=Some(field.index), store=Some(field.store))))
  //      case _ => extractType(clazz)
  //
  //
  //    }
  //  }

}
