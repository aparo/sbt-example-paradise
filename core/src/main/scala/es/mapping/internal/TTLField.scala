package es.mapping.internal

import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import es.mapping.Field

class TTLField(var enabled: Option[Boolean] = None, var default: Option[String] = None, path: Option[String]= None) extends Field {
  override val `type`: String = "_ttl"
}
