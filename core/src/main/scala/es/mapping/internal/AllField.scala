package es.mapping.internal

import es.mapping.Field
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonDSL._

/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 05/03/13
 * Time: 17:49
 */
class AllField extends Field {
  var enabled: Boolean = true
  override val `type`: String = "_all"

  override def toJson: JObject = ("enabled" -> enabled)
}
