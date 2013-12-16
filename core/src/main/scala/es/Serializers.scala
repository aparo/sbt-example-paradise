package es

import scala.language.implicitConversions
import net.liftweb.json.Serializer
import net.liftweb.json.ext.JodaTimeSerializers
import net.liftweb.json.Extraction._
import es.client.responses._
import es.queries.JsonQuery
import net.liftweb.json.JsonAST.JObject
import es.mapping.MappingSerializers
import es.client.OperationSerializers
import es.search.{QueryFilterCachable, Search}


object ESSerializers {


  def all = List(QuerySerializer) ++ MappingSerializers.all ++ OperationSerializers.all
}

object QuerySerializer extends Serializer[QueryFilterCachable] {

  import net.liftweb.json.JsonAST.JValue
  import net.liftweb.json.{TypeInfo, Formats}


  private val QueryClass = classOf[QueryFilterCachable]
  private implicit val base_formats = net.liftweb.json.DefaultFormats ++ JodaTimeSerializers.all

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), QueryFilterCachable] = {
    case (TypeInfo(QueryClass, _), json) => json match {
      case x => new JsonQuery(x.asInstanceOf[JObject])
    }
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case q: JsonQuery => q.toJson
    case q: QueryFilterCachable => q.toJson
    case s: Search => s.toJson
  }
}

