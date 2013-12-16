package es.search

import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.liftweb.json.ext.JodaTimeSerializers
import es.orm.Model
import es.client.{ESBaseCursor, Client}
import net.liftweb.json.Extraction._
import es.client.responses.SearchResponse
import es.DefaultESIdentifier
import es.search.sort._

trait Query extends QueryFilterCachable {
  def boost: Option[Float]

  def toInnerJson = ("_name" -> _name.map(_.toString)) ~ ("boost" -> boost)

  def toMap

}

trait Filter extends QueryFilterCachable with Model {
  def _cache: Option[Boolean]

  def _cache_key: Option[String]

  def toInnerJson: JObject = ("_name" -> _name.map(_.toString)) ~
    ("_cache" -> _cache.map(_.toString)) ~
    ("_cache_key" -> _cache_key.map(_.toString)
      )
}

trait QueryFilterCachable {
  var _name: Option[String] = None

  val NAME = "undefined"

  implicit val formats = DefaultESIdentifier.formats
  implicit val base_formats = net.liftweb.json.DefaultFormats ++ JodaTimeSerializers.all

  def toInnerJson: JObject

  def toJson: JObject = (NAME -> toInnerJson)

  def toJsonString: String = compact(render(toJson))

}


case class Search(query: Query, fields: List[String] = Nil,
                  from: Int = 0, size: Int = -1, highlight: List[String] = Nil,
                  explain: Boolean = false, facets: Map[String, Facet] = Map.empty[String, Facet],
                  bulkRead: Int = -1,
                  sort: Sort = EmptySort, searchType: Option[String] = None, scrollTime: Option[String] = None,
                  timeout: Long = 0, version: Boolean = true,
                  trackScore: Boolean = false) {

  implicit val formats = DefaultESIdentifier.formats
  val defaultScrollTime:String = "1m"

  def toJson: JObject = {
    //TODO adde new values
    var json =
      ("query" -> query.toJson) ~
        ("from" -> from) ~
        ("size" -> size) ~
        ("version" -> version)
    if (!fields.isEmpty)
      json ~= ("fields" -> fields)
    if (!facets.isEmpty) {
      json ~= ("facets" -> facets.map {
        case (name, facet) =>
          name -> facet.toJson
      })
    }
    if (explain)
      json ~= ("explain" -> explain)
    if (trackScore)
      json ~= ("track_score" -> trackScore)
    if (!sort.isEmpty) {
      json ~= ("sort" -> decompose(sort)(formats))
    }
    json
  }

  def setScan(scrollTime: String = "1m"): Search = {
    if (isScan) {
      var result = this.copy(searchType = Some("scan"))
      if (!result.scrollTime.isDefined)
        result = result.copy(scrollTime = Some(scrollTime))
      return result
    }
    this
  }

  def getQueryParameters: Map[String, String] = {
    var parameters = Map.empty[String, String]
    if (isScan){
      val scroll:String = this.scrollTime match {
        case None => this.defaultScrollTime
        case Some(s) => s
      }
      return Map("search_type" -> "scan", "scroll" -> scroll)
    }
    if (searchType.isDefined)
      parameters += ("search_type" -> searchType.get)
    if (scrollTime.isDefined)
      parameters += ("scroll" -> scrollTime.get)
    parameters
  }

  def isScan: Boolean = {
    if (!facets.isEmpty)
      return false
    searchType match {
      case Some("scan") => return true
      case _ =>
    }
    if (!this.sort.isEmpty)
      return false
    if (sort.isEmpty)
      return true

    false
  }

  def addFacet(facets: (String, Facet)*): Search = this.copy(facets = this.facets ++ facets)

  def getResponse(client: Client, indices: Seq[String], docTypes: Seq[String],
                  params: Map[String, String]): SearchResponse = {
    implicit val formats = DefaultESIdentifier.formats
    val json = client.searchRaw(this, indices = indices, docTypes = docTypes, params = params)
    json.extract[SearchResponse]
  }

  def execute(client: Client, indices: Seq[String], docTypes: Seq[String],
              params: Map[String, String]): ESBaseCursor = {

    client.search(this, indices = indices, docTypes = docTypes, params = params)
  }
}

object QueryImplicits {
  implicit def Query2Search(query: Query) = new Search(query)
}

