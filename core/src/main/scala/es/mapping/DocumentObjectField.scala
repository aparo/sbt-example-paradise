package es.mapping

import internal._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json.Extraction.decompose
import es.DefaultESIdentifier
import es.client.responses.HitResponse

class DocumentObjectField extends BaseObjectField {
  //used in queyset set by Mapper
  var indexName:String=""
  var typeName:String=""

  var _all: Option[AllField] = None
  var _boost: Option[BoostField] = None
  var _id: Option[IdField] = None
  var _index: Option[IndexField] = None
  var _source: Option[SourceField] = None
  var _type: Option[TypeField] = None
  var _routing: Option[RoutingField] = None
  var _ttl: Option[TTLField] = None
  var _parent: Option[ParentField] = None
  var _timestamp: Option[TimeStampField] = None
  var _size: Option[SizeField] = None
  var _analyzer: Option[String] = None
  var date_detection: Option[Boolean] = None
  var numeric_detection: Option[Boolean] = None
  var dynamic_date_formats: Option[List[String]] = None
  var date_formats: Option[List[String]] = None
  var _meta: Option[Map[String, Any]] = None



    override def read(in: Map[String, Any]) {
      super.read(in)
      if (in.contains("_all")) {
        val data = in("_all").asInstanceOf[Map[String, AnyRef]]
        val s = new AllField()
        s.read(data)
        _all = Some(s)

      }

      if (in.contains("_ttl")) {
        val data = in("_ttl").asInstanceOf[Map[String, AnyRef]]
        val s = new TTLField()
        s.read(data)
        _ttl = Some(s)

      }

      if (in.contains("_source")) {
        val data = in("_source").asInstanceOf[Map[String, AnyRef]]
        val s = new SourceField()
        s.read(data)
        _source = Some(s)

      }

      if (in.contains("_parent")) {
        val data = in("_parent").asInstanceOf[Map[String, AnyRef]]
        val s = new ParentField()
        s.read(data)
        _parent = Some(s)

      }

      if (in.contains("_size")) {
        val data = in("_size").asInstanceOf[Map[String, AnyRef]]
        val s = new SizeField()
        s.read(data)
        _size = Some(s)
      }

      if (in.contains("_timestamp")) {
        val data = in("_timestamp").asInstanceOf[Map[String, AnyRef]]
        val s = new TimeStampField()
        s.read(data)
        _timestamp = Some(s)
      }

      if (in.contains("_boost")) {
        val data = in("_boost").asInstanceOf[Map[String, AnyRef]]
        val s = new BoostField()
        s.read(data)
        _boost = Some(s)
      }

      if (in.contains("_id")) {
        val data = in("_id").asInstanceOf[Map[String, AnyRef]]
        val s = new IdField()
        s.read(data)
        _id = Some(s)
      }

      if (in.contains("_index")) {
        val data = in("_index").asInstanceOf[Map[String, AnyRef]]
        val s = new IndexField()
        s.read(data)
        _index = Some(s)
      }

      if (in.contains("_type")) {
        val data = in("_type").asInstanceOf[Map[String, AnyRef]]
        val s = new TypeField()
        s.read(data)
        _type = Some(s)
      }


      if (in.contains("_routing")) {
        val data = in("_routing").asInstanceOf[Map[String, AnyRef]]
        val s = new RoutingField()
        s.read(data)
        _routing = Some(s)
      }

      if (in.contains("_analyzer"))
        _analyzer = Some(in("_analyzer").asInstanceOf[String])
      if (in.contains("date_detection"))
        date_detection = Some(in("date_detection").asInstanceOf[Boolean])
      if (in.contains("numeric_detection"))
        numeric_detection = Some(in("numeric_detection").asInstanceOf[Boolean])
      if (in.contains("dynamic_date_formats"))
        dynamic_date_formats = Some(in("dynamic_date_formats").asInstanceOf[List[String]])
      if (in.contains("date_formats"))
        date_formats = Some(in("date_formats").asInstanceOf[List[String]])
      if (in.contains("_meta"))
        _meta = Some(in("_meta").asInstanceOf[Map[String, Any]])
    }

    override def toJson: JObject = {
      var json = super.toJson ~
        ("date_detection" -> date_detection) ~
        ("_analyzer" -> _analyzer) ~
        ("numeric_detection" -> numeric_detection) ~
        ("dynamic_date_formats" -> dynamic_date_formats) ~
        ("date_formats" -> date_formats)
      if (_all.isDefined) json = json ~ ("_all" -> _all.get.toJson)
      if (_boost.isDefined) json = json ~ ("_boost" -> _boost.get.toJson)
      if (_id.isDefined) json = json ~ ("_id" -> _id.get.toJson)
      if (_index.isDefined) json = json ~ ("_index" -> _index.get.toJson)
      if (_source.isDefined) json = json ~ ("_source" -> _source.get.toJson)
      if (_type.isDefined) json = json ~ ("_type" -> _type.get.toJson)
      if (_routing.isDefined) json = json ~ ("_routing" -> _routing.get.toJson)
      if (_ttl.isDefined) json = json ~ ("_ttl" -> _ttl.get.toJson)
      if (_parent.isDefined) json = json ~ ("_parent" -> _parent.get.toJson)
      if (_timestamp.isDefined) json = json ~ ("_timestamp" -> _timestamp.get.toJson)
      if (_size.isDefined) json = json ~ ("_size" -> _size.get.toJson)
      if (_meta.isDefined && !_meta.get.isEmpty) {
        implicit val formats = DefaultESIdentifier.formats
        json = json ~ ("_meta" -> _meta.get.map {
          case (k, v) => (k -> decompose(v))
        })
      }

      json
    }

  def lambaHit(hit:HitResponse):HitResponse = hit

}


