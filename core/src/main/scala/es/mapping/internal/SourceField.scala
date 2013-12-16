package es.mapping.internal

import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import es.mapping.Field

class SourceField(name: Option[String]= None, enabled: Option[Boolean] = None, compress: Option[Boolean] = None, path: Option[String]= None) extends Field {
  override val `type`: String = "_source"

  override def toJson: JObject = ("enabled" -> enabled) ~ ("compress" -> compress)

}
