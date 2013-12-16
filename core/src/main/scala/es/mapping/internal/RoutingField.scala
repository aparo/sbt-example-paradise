package es.mapping.internal

import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import es.mapping.Field

class RoutingField(var required: Option[Boolean] = None, path: Option[String]= None) extends Field {
  override val `type`: String = "_routing"

  //override def toJson: JObject = ("name" -> name) ~ ("required" -> required)

}
