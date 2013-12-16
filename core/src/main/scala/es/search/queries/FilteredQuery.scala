package es.queries


import net.liftweb.json.Extraction._
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonDSL._
import scala.language.implicitConversions
import es.utils.Mapper
import es.search.{QueryFilterCachable, Filter, Query}


case class FilteredQuery(query: Query, filter: Filter, boost: Option[Float] = None) extends Query {
  override val NAME = "filtered"

  override def toInnerJson:JObject =
    super.toInnerJson ~
      ("query" -> query.toJson) ~
      ("filter" -> filter.toJson) ~
      ("boost" -> boost.map(_.toFloat))

  override def toMap=Mapper.toMap(this)

}

object MatchAllQuery{
  def apply()=new MatchAllQuery()
}

case class MatchAllQuery(var normsField: Option[String] = None, boost: Option[Float] = None) extends Query {
  override val NAME = "match_all"

  override def toInnerJson: JObject = super.toInnerJson ~ ("norms_field" -> normsField)

  override def toMap=Mapper.toMap(this)


}

case class JsonQuery(json: JObject,
                     boost: Option[Float] = None ) extends Query {
  override val NAME = "term"

  override def toJson: JObject = this.json

  override def toMap=Mapper.toMap(this)

}
