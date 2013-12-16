package es.utils

import com.thoughtworks.paranamer.{BytecodeReadingParanamer, CachingParanamer}
import java.util.Date
import scala.reflect.ClassTag

/**
 * Created by alberto on 25/10/13.
 */
object Mapper {
  lazy val pn = new CachingParanamer(new BytecodeReadingParanamer)
  val SString = classOf[String]
  val JString = classOf[java.lang.String]
  val SBoolean = classOf[Boolean]
  val JBoolean = classOf[java.lang.Boolean]
  val JInteger = classOf[Int]
  val JavaInteger = classOf[java.lang.Integer]
  val JLong = classOf[Long]
  val JDate = classOf[Date]
  val JClass = classOf[java.lang.Class[_]]

  private def getValue(value:Any):Any={
    value.getClass match {
      case JClass => value.asInstanceOf[Class[_]].getCanonicalName
      case JInteger => value
      case JavaInteger => value
      case JLong => value
      case JString => value
      case SString => value
      case JBoolean => value
      case SBoolean => value
      case JDate => value
      case _ =>
        toMap(value)
    }
  }

  def toMap[T](cc: T)(implicit mf: ClassTag[T]):Map[String, Any] ={
    var result:Map[String, Any]=Map.empty[String, Any]
    for {
      ctor <- mf.runtimeClass.getDeclaredConstructors.headOption
      parameters = pn.lookupParameterNames(ctor)
    } {
      val types = parameters.zip(ctor.getGenericParameterTypes).toMap
      cc.getClass.getDeclaredFields.foreach{f =>
        f.setAccessible(true)
        val name = f.getName
//        println(name)
        //        if(types.contains(name)){
        val value = f.get(cc)
        value match {
          case None =>
          case null =>
          case Nil =>List()
          case Some(v) =>
            result += (name -> getValue(v))
          case x:Seq[_] =>
            result += (name -> x.map(v=> getValue(v)))
          case _ if name=="MODULE$"=>
            result += (name -> value.toString)
          case _ =>
            result += (name -> getValue(value))
        }
        //          }
      }
    }
    result
  }

  def toMap2[T](cc: T)(implicit mf: ClassTag[T]):Map[String, Any] ={
    var result:Map[String, Any]=Map.empty[String, Any]
    for {
      ctor <- mf.runtimeClass.getDeclaredConstructors.headOption
      parameters = pn.lookupParameterNames(ctor)
    } {
      val types = parameters.zip(ctor.getGenericParameterTypes).toMap
      cc.getClass.getDeclaredFields.foreach{f =>
        f.setAccessible(true)
        val name = f.getName
        //        if(types.contains(name)){
        val value = f.get(cc)
        value match {
          case None =>
          case Some(v) =>
            result += (name -> getValue(v))

          case _ =>
            result += (name -> getValue(value))
        }
        //          }
      }
    }
    result
  }

}
