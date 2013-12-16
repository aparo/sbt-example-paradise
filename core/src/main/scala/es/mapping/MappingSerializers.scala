package es.mapping

import net.liftweb.json._
import net.liftweb.json.JsonAST.JValue
import es.client.responses.HitResponse
import net.liftweb.json.TypeInfo
import scala.Some
import es.client.responses.HitResponse
import org.joda.time.DateTime
import java.lang.{Long => JLong}
import net.liftweb.json.Extraction._
import net.liftweb.json.TypeInfo
import scala.Some
import es.client.responses.HitResponse
import es.client.responses.admin.cluster.{ESIndices, ESNode}

/**
 * Created by ivan on 22/11/13.
 */
object MappingSerializers {


  def all = List(DocumentObjectFieldSerializers,NodeSerializers)
}


object DocumentObjectFieldSerializers extends Serializer[DocumentObjectField] {

  import net.liftweb.json._


  private val DocClass = classOf[DocumentObjectField]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), DocumentObjectField] = {
    case (TypeInfo(DocClass, _), json) =>
      val doc = new DocumentObjectField()
      doc.read(json.values.asInstanceOf[Map[String, Any]])
      doc
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case q: DocumentObjectField => q.toJson
  }
}


object NodeSerializers extends Serializer[ESNode] {

  import net.liftweb.json._

  private val NodeClass = classOf[ESNode]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), ESNode] = {
    case (TypeInfo(NodeClass, _), json) => {
      val nod = new ESNode(
        name = (json \ "name").extract[String],
        transport_address = (json \ "transport_address").extract[String],
        attributes = (json \ "attributes") match {
          case JNothing => Some(Map.empty[String, Any])
          case jobj: JObject => Some(jobj.values)
          case _ => Some((json \ "attributes").asInstanceOf[JObject].values)
        },
        hostname = (json \ "hostname") match {
          case JNothing => None
          case jobj: JString => Some(jobj.values)
          case _ => None
        },
        timestamp = (json \ "timestamp") match {
          case JNothing => None
          case jobj: JInt => Some(new DateTime(JLong.parseLong(jobj.num.toString())))
          case _ => None
        },
        indices = (json \ "indices") match {
          case JNothing => None
          case jobj: JObject => Some(jobj.extract[ESIndices])
          case _ => None
        }
      )
      nod
    }

  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case q: DocumentObjectField => q.toJson

  }
}

//case (TypeInfo(QueryClass, _), json) =>
//HitResponse(
//index=(json \ "_index").extract[String],
//docType=(json \ "_type").extract[String],
//id=(json \ "_id").extract[String],
//version=Some((json \ "_version").extract[Long]),
//score=Some((json \ "_score").extract[Double]),
//source = (json \ "_source"),
//fields = (json \ "_source") match {
//case JNothing=> Map.empty[String, Any]
//case jobj:JObject => jobj.values
//case _ => (json \ "_source").asInstanceOf[JObject].values
//}
//)