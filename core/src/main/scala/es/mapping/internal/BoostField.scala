package es.mapping.internal

import es.DefaultESIdentifier
import es.mapping.Field
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 05/03/13
 * Time: 17:49
 */
class BoostField extends Field {
  var null_value: Option[Float] = None
  var name: Option[String]= None

  override val `type`: String = "_boost"

  //TODO: E' da eliminare?
//  override def toJson: JObject = {
//    implicit val formats = DefaultESIdentifier.formats
//    if (null_value.isDefined) {
//      return ("name" -> name) ~ ("null_value" -> decompose(null_value))
//    }
//    ("name" -> name)
//  }
}
