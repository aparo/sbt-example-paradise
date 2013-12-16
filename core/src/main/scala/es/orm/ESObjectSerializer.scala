package es.orm

import net.liftweb.json.CustomSerializer
import net.liftweb.json.JsonAST._

case class FieldSerializer[A: Manifest](
                                         serializer: PartialFunction[(String, Any), Option[(String, Any)]] = Map(),
                                         deserializer: PartialFunction[JField, JField] = Map()
                                         )
