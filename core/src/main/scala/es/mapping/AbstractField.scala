package es.mapping

import es.DefaultESIdentifier
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._

abstract class AbstractField extends Field {
  var index: Option[String] = None
  var store: Option[Boolean] = None
  var boost: Option[Double]=Some(1.0)
  var required: Option[Boolean] = None
  var multiple: Option[Boolean] = None
  var index_name: Option[String] = None
  var index_options: Option[String] = None
  var analyzer: Option[String] = None
  var index_analyzer: Option[String] = None
  var search_analyzer: Option[String] = None
  var permission: Option[List[String]] = None
  var meta: Option[Map[String, Any]] = None

  override def toJson: JObject = {
    var json = super.toJson
    json = json ~ ("index" -> index) ~
      ("store" -> store) ~
      ("required" -> required) ~
      ("multiple" -> multiple) ~
      ("index_name" -> index_name) ~
      ("index_options" -> index_options) ~
      ("analyzer" -> analyzer) ~
      ("index_analyzer" -> index_analyzer) ~
      ("search_analyzer" -> search_analyzer) ~
      ("permission" -> permission)

    if (boost.getOrElse(1.0) != 1.0) {
      json = json ~ ("boost" -> boost)
    }

    if (meta.size > 0) {
      implicit val formats = DefaultESIdentifier.formats
      json = json ~ ("meta" -> decompose(meta))
    }
    json
  }



    override def read(in: Map[String, Any]) {
      super.read(in)
      if (in.contains("index"))
        index = in("index") match {
          case s: String => Some(s)
          case _ => None
        }

      if (in.contains("store")) {
        store = in("store") match {
          case s: String => Some(toBoolean(s))
          case b: Boolean => Some(b)
        }
      }
          if (in.contains("required"))
            required = Some(in("required").asInstanceOf[Boolean])
          if (in.contains("multiple"))
            multiple = Some(in("multiple").asInstanceOf[Boolean])

          if (in.contains("index_name"))
            index_name = Some(in("index_name").asInstanceOf[String])
          if (in.contains("index_options"))
            index_options = Some(in("index_options").asInstanceOf[String])
          if (in.contains("analyzer"))
            analyzer = Some(in("analyzer").asInstanceOf[String])
          if (in.contains("index_analyzer"))
            index_analyzer = Some(in("index_analyzer").asInstanceOf[String])
          if (in.contains("search_analyzer"))
            search_analyzer = Some(in("search_analyzer").asInstanceOf[String])
          if (in.contains("permission"))
            permission = Some(in("permission").asInstanceOf[List[String]])
//          if (in.contains("meta"))
//            meta = in("meta").asInstanceOf[Map[String, Any]]

    }

}
