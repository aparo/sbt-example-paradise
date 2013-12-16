package es.client

import net.liftweb.json._
import es.client.responses._
import net.liftweb.json.Extraction._
import es.client.responses.facet._
import es.client.responses._
import scala.Some
import es.client.responses.HitResponse
import org.joda.time.DateTime

object OperationSerializers {


  def all = List(SearchResponseSerializer, HitResponseSerializer, FacetSerializer, TermSerializer, RangeSerializer,
    HistogramSerializer, DateHistogramSerializer, GetResponseSerializer)
}


object SearchResponseSerializer extends Serializer[SearchResponse] {

  import net.liftweb.json._

  private val QueryClass = classOf[SearchResponse]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), SearchResponse] = {
    case (TypeInfo(QueryClass, _), json) =>
      SearchResponse(
        took=(json \ "took").extract[Double],
        timedOut=(json \ "timed_out").extract[Boolean],
        shards=(json \ "_shards").extract[Shards],
        hits=(json \ "hits") match {
          case JNothing=> Hits(0)
          case f:JObject=> f.extract[Hits]
          case _ => (json \ "hits").extract[Hits]
        },
        facets=(json \ "facets") match {
          case JNothing=> EmptyFacets
          case f:JObject=> f.extract[Facets]
          case _ => EmptyFacets
        },
      scrollId=(json \ "_scroll_id") match {
        case JNothing=> None
        case JString(id)=> Some(id)
        case _ => None
      }
      )
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case q: SearchResponse => decompose(q)(formats)
  }
}

object FacetSerializer extends Serializer[Facet] {

  import net.liftweb.json._


  private val QueryClass = classOf[Facet]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Facet] = {
    case (TypeInfo(QueryClass, _), json) =>
      (json \ "_type").extract[String] match {
        case "terms" =>
          json.extract[TermsFacet]
        case "date_histogram" => json.extract[DateHistogramFacet]
        case "histogram" => json.extract[HistogramFacet]
        case "terms_stats" => json.extract[TermsStatsFacet]
        case "statistical" => json.extract[StatisticalFacet]
        case "range" => json.extract[RangeFacet]
        case "geo_distance" => json.extract[RangeFacet]
        case "filter" => json.extract[FilterFacet]

        }
  }

  //"_index":"testkit_es_orm_querysetspec","_type":"es_fixures_Person","_id":"1001","_version":1,"_score":1.0,
  // "_source" : {"age":1001,"children":[],"metadata":{}}},{"_index":"testkit_es_orm_querysetspec",
  // "_type":"es_fixures_Person","_id":"1000","_version":1,"_score":1.0, "_source" : {"age":1000,"children":[],
  // "metadata":{"metadata_label_fat":{"name":"metadata_name_fat"}}}}

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case q: HitResponse => decompose(q)(formats)
  }
}


object HitResponseSerializer extends Serializer[HitResponse] {

  import net.liftweb.json._


  private val QueryClass = classOf[HitResponse]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), HitResponse] = {
    case (TypeInfo(QueryClass, _), json) =>
      HitResponse(
        index=(json \ "_index").extract[String],
        docType=(json \ "_type").extract[String],
        id=(json \ "_id").extract[String],
        version=(json \ "_version") match {
          case JNothing=> None
          case JInt(i) => Some(i.toLong)
          case _ => None
        },
        score=Some((json \ "_score").extract[Double]),
        source = (json \ "_source"),
        fields = (json \ "fields") match {
          case JNothing=> Map.empty[String, Any]
          case jobj:JObject => jobj.values
          case _ => (json \ "_source").asInstanceOf[JObject].values
        }
      )
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case q: HitResponse => decompose(q)(formats)
  }
}

object GetResponseSerializer extends Serializer[GetResponse] {

  import net.liftweb.json._


  private val QueryClass = classOf[GetResponse]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), GetResponse] = {
    case (TypeInfo(QueryClass, _), json) =>
      GetResponse(
        index=(json \ "_index").extract[String],
        docType=(json \ "_type").extract[String],
        id=(json \ "_id").extract[String],
        version=(json \ "_version") match {
          case JNothing=> -1
          case JInt(i) => i.toLong
          case _ => -1
        },
        source = (json \ "_source"),
        exists = (json \ "exists") match {
          case JNothing=> false
          case JBool(value) => value
          case _ => false
        }
      )
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case q: GetResponse => decompose(q)(formats)
  }
}

object TermSerializer extends Serializer[Term] {

  import net.liftweb.json._


  private val QueryClass = classOf[Term]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Term] = {
    case (TypeInfo(QueryClass, _), json) =>
      val map=json.values.asInstanceOf[Map[String, Any]]
      Term(
        term=map.getOrElse("term", "Na"),
        count=(json \ "count").extract[Long]
      )
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case q: Term => decompose(q)(formats)
  }
}


object DateHistogramSerializer extends Serializer[DateHistogramEntry] {

  import net.liftweb.json._

  private val QueryClass = classOf[DateHistogramEntry]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), DateHistogramEntry] = {
    case (TypeInfo(QueryClass, _), json) =>
      val map=json.values.asInstanceOf[Map[String, Any]]
      val time=(json \ "time").extract[Long]
      DateHistogramEntry(
        time=new DateTime(time),
        count=(json \ "count").extract[Long],
        min=(json \ "min") match {
          case JNothing=> None
          case _ => Some(map.get("min").get.asInstanceOf[Double])
        },
        max=(json \ "max") match {
          case JNothing=> None
          case _ => Some(map.get("max").get.asInstanceOf[Double])
        },
        total=(json \ "total") match {
          case JNothing=> None
          case _ => Some(map.get("total").get.asInstanceOf[Double])
        },
        totalCount=(json \ "total_count") match {
          case JNothing=> None
          case _ => Some(map.get("total_count").get.asInstanceOf[BigInt].toLong)
        },
        mean=(json \ "mean") match {
          case JNothing=> None
          case _ => Some(map.get("mean").get.asInstanceOf[Double])
        }
      )
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case q: DateHistogramEntry => decompose(q)(formats)
  }
}

object SettingsSerializer extends Serializer[Settings] {

  import net.liftweb.json._

  private val QueryClass = classOf[HistogramEntry]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Settings] = {
    case (TypeInfo(QueryClass, _), json) =>
      val map=json.values.asInstanceOf[Map[String, Any]]
      Settings() //TODO finish serializer
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case q: Settings =>
      decompose(q.options ++ Map("number_of_shards" -> q.shards, "number_of_replicas" -> q.replicas))(formats)
  }
}


object HistogramSerializer extends Serializer[HistogramEntry] {

  import net.liftweb.json._

  private val QueryClass = classOf[HistogramEntry]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), HistogramEntry] = {
    case (TypeInfo(QueryClass, _), json) =>
      val map=json.values.asInstanceOf[Map[String, Any]]
      HistogramEntry(
        key=map.getOrElse("key", "Na"),
        count=(json \ "count").extract[Long]
      )
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case q: HistogramEntry => decompose(q)(formats)
  }
}

object RangeSerializer extends Serializer[Range] {

  import net.liftweb.json._


  private val QueryClass = classOf[Range]
  //case class Range(from:Option[Any]=None, to:Option[Any]=None, count:Long, totalCount:Long, min:Double, max:Double, total:Double, mean:Double)

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Range] = {
    case (TypeInfo(QueryClass, _), json) =>
      val map=json.values.asInstanceOf[Map[String, Any]]
      Range(
        from=(json \ "from") match {
          case JNothing=> None
          case _ => Some(map.get("from").get)
        },
        to=(json \ "to") match {
          case JNothing=> None
          case _ => Some(map.get("to").get)
        },
        count=(json \ "count").extract[Long],
        totalCount=(json \ "total_count").extract[Long],
        total=(json \ "total").extract[Double],
        mean=(json \ "mean").extract[Double],
        min=(json \ "min")match {
          case JNothing=> 0
          case JDouble(v) => v
          case JString(s) => s match{
            case "-Infinity" => Double.MinValue
            case "Infinity" => Double.MaxValue
          }
          case _ => 0
        },
        max=(json \ "max")match {
          case JNothing=> 0
          case JDouble(v) => v
          case JString(s) => s match{
            case "-Infinity" => Double.MinValue
            case "Infinity" => Double.MaxValue
            case _ => Double.MaxValue
          }
          case _ => 0
        }
      )
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case q: Range => decompose(q)(formats)
  }
}

