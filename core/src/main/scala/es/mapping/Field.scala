package es.mapping

import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import com.thoughtworks.paranamer.{BytecodeReadingParanamer, CachingParanamer}

abstract class Field() {
  var path: Option[String] = None
  val `type`: String = "type"

  def toJson: JObject = ("path" -> path) ~ ("type" -> `type`)

  def toBoolean(s: String): Boolean = s match {
    case "true" => true
    case "false" => false
    case "yes" => true
    case "no" => false
    case "analyzed" => true
    case "not_analyzed" => false
  }

  def read(in: Map[String, Any]) {
    if (in.contains("path"))
      path = in("path") match {
        case s: String => Some(s)
        case _ => None
      }
  }

  //class to map
  val pn = new CachingParanamer(new BytecodeReadingParanamer)

  def fill[T](m: Map[String, AnyRef])(implicit mf: scala.reflect.ClassTag[T]) = for {
    ctor <- mf.runtimeClass.getDeclaredConstructors.filter(m => m.getParameterTypes.forall(classOf[String] ==)).headOption
    parameters = pn.lookupParameterNames(ctor)
  } yield ctor.newInstance(parameters.map(m): _*).asInstanceOf[T]

}