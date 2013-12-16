package es.mapping.internal
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import es.mapping.Field

class SizeField(var index: Option[Boolean] = None, var store: Option[Boolean] = None, path: Option[String]= None) extends Field {
  override val `type`: String = "_size"

  //override def toJson: JObject = ("name" -> name) ~ ("index" -> index) ~ ("store" -> store)

}
