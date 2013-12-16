package es.mapping.internal

import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import es.mapping.{AbstractField, Field}

/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 05/03/13
 * Time: 17:53
 */
//class IdField(name: Option[String] = None, index: Option[String] = None, store: Option[Boolean] = None,
//                   boost: Option[Double]=Some(1.0), required: Option[Boolean] = None,
//                   multiple: Option[Boolean] = None, index_name: Option[String] = None,
//                   index_options: Option[String] = None, analyzer: Option[String] = None,
//                   index_analyzer: Option[String] = None, search_analyzer: Option[String] = None,
//                   permission: Option[List[String]] = None, meta: Option[Map[String, Any]] = None,
//                   path: Option[String] = None
//                    ) extends AbstractField(index, store, boost, required, multiple, index_name, index_options, analyzer,
//  index_analyzer, search_analyzer, permission, meta, path) {
//  override val `type`: String = "_id"
//
//  //TODO: E' da eliminare?
//  //  override def toJson: JObject = ("name" -> name) ~ ("index" -> index) ~ ("store" -> store)
//
//}
class IdField(name: Option[String] = None,index: Option[Boolean] = None, store: Option[Boolean] = None, path: Option[String]= None) extends Field {
  override val `type`: String = "_id"

}