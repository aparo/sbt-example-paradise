package es.mapping.internal

import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import es.mapping.Field

/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 05/03/13
 * Time: 17:50
 */
class IndexField(var enabled: Option[Boolean] = None, path: Option[String]= None) extends Field {
  override val `type`: String = "_index"

}
