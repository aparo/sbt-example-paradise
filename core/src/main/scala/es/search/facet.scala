package es.search

import es.utils._
import net.liftweb.json.ext.JodaTimeSerializers
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Extraction._

case class RangeForFacet(from: Option[Any] = None, to: Option[Any] = None)

trait Facet {

  var scope: Option[String]

  var nested: Option[Boolean]

  var isGlobal: Option[Boolean]

  var facetFilter: Option[Filter]

  def internalName: String

  protected def serialize: JObject = {

    var json = ("scope" -> scope) ~ ("nested" -> nested) ~ ("global" -> isGlobal)
    if (facetFilter.isDefined)
      json ~= ("facet_filter" -> facetFilter.get.toInnerJson)
    json
  }

  def toJson: JObject = internalName -> serialize

  def toMap=Map.empty[String, Any]
}


case class QueryFacet(query: Query,
                      var scope: Option[String] = None, var nested: Option[Boolean] = None,
                      var isGlobal: Option[Boolean] = None, var facetFilter: Option[Filter] = None) extends Facet {

  def internalName = "query"

  override protected def serialize: JObject = super.serialize ~ ("query" -> query.toJson)

  override def toMap = Map(internalName -> super.toMap)

}

case class FilterFacet(filter: Filter,
                       var scope: Option[String] = None, var nested: Option[Boolean] = None,
                       var isGlobal: Option[Boolean] = None, var facetFilter: Option[Filter] = None) extends Facet {

  def internalName = "filter"

  override protected def serialize: JObject = super.serialize ~ ("filter" -> filter.toJson)

  override def toMap = Map(internalName -> super.toMap)

}

object HistogramFacet {
  def apply(field:String)=new HistogramFacet(field=Some(field))
}

case class HistogramFacet(keyField: Option[String] = None, valueField: Option[String] = None, field: Option[String] = None,
                          interval: Option[Long] = None, timeInterval: Option[String] = None, keyScript: Option[String] = None,
                          valueScript: Option[String] = None, params: Map[String, Any] = Map.empty[String, Any],
                          var scope: Option[String] = None, var nested: Option[Boolean] = None,
                          var isGlobal: Option[Boolean] = None, var facetFilter: Option[Filter] = None) extends Facet {

  def internalName = "histogram"


  override protected def serialize: JObject = {

    var json = super.serialize
    implicit val base_formats = DefaultFormats ++ JodaTimeSerializers.all

    if (field.isDefined)
      json ~= ("field" -> field)
    else if (keyField.isDefined) {
      json ~= ("key_field" -> keyField)
      if (valueField.isDefined)
        json ~= ("value_field" -> valueField)
      else
        throw new IllegalArgumentException("Invalid key_field: value_field required");
    }
    else if (keyScript.isDefined) {
      json ~= ("key_script" -> keyScript)
      if (valueScript.isDefined)
        json ~= ("value_script" -> valueField) ~ ("params" -> decompose(params)(base_formats))
      else
        throw new IllegalArgumentException("Invalid key_script: value_script required");
    }

    if (field.isDefined || keyField.isDefined || keyScript.isDefined) {
      if (interval.isDefined)
        json ~= ("interval" -> interval)
      else if (timeInterval.isDefined)
        json ~= ("time_interval" -> timeInterval)
      else
        throw new IllegalArgumentException("Invalid field: interval or time_interval required");
    }
    json
  }


  override def toMap: Map[String, Any] = {

    var map: Map[String, Any] = Map.empty[String, Any]

    if (scope.isDefined)
      map = map ++ Map("scope" -> scope.get)
    if (nested.isDefined)
      map = map ++ Map("nested" -> nested.get)
    if (isGlobal.isDefined)
      map = map ++ Map("global" -> isGlobal.get)
    if (facetFilter.isDefined)
      map = map ++ Map("facet_filter" -> facetFilter.get)

    if (field.isDefined)
      map = map ++ Map("field" -> field.get)
    else if (keyField.isDefined) {
      map = map ++ Map("key_field" -> keyField.get)
      if (valueField.isDefined)
        map = map ++ Map("value_field" -> valueField.get)
      else
        throw new IllegalArgumentException("Invalid key_field: value_field required");
    }
    else if (keyScript.isDefined) {
      map = map ++ Map("key_script" -> keyScript.get)
      if (valueScript.isDefined)
        map = map ++ Map("value_script" -> valueField.get) ++ Map("params" -> params)
      else
        throw new IllegalArgumentException("Invalid key_script: value_script required");
    }

    if (field.isDefined || keyField.isDefined || keyScript.isDefined) {
      if (interval.isDefined)
        map = map ++ Map("interval" -> interval.get)
      else if (timeInterval.isDefined)
        map = map ++ Map("time_interval" -> timeInterval.get)
      else
        throw new IllegalArgumentException("Invalid field: interval or time_interval required");
    }
    Map(internalName -> map)
  }


}

object DateHistogramFacet {
  def apply(field:String)=new DateHistogramFacet(field=Some(field), interval = DateKind.Day)
}

case class DateHistogramFacet(field: Option[String] = None, interval: DateKind, keyField: Option[String] = None,
                              valueField: Option[String] = None, keyScript: Option[String] = None, valueScript: Option[String] = None,
                              params: Map[String, Any] = Map.empty[String, Any],
                              var scope: Option[String] = None, var nested: Option[Boolean] = None,
                              var isGlobal: Option[Boolean] = None, var facetFilter: Option[Filter] = None) extends Facet {
  implicit val base_formats = DefaultFormats ++ JodaTimeSerializers.all

  def internalName = "date_histogram"

  override protected def serialize: JObject = {

    var json = super.serialize ~ ("interval" -> interval.name)

    if (field.isDefined)
      json ~= ("field" -> field)
    else if (keyField.isDefined) {
      json ~= ("key_field" -> keyField)
      if (valueField.isDefined)
        json ~= ("value_field" -> valueField)
      else
        throw new IllegalArgumentException("Invalid key_field: value_field required");
    }
    else if (keyScript.isDefined) {
      json ~= ("key_script" -> keyScript)
      if (valueScript.isDefined)
        json ~= ("value_script" -> valueField) ~ ("params" -> decompose(params)(base_formats))
      else
        throw new IllegalArgumentException("Invalid key_script: value_script required");
    }
    json
  }

  override def toMap: Map[String, Any] = {


    var map: Map[String, Any] = Map("interval" -> interval.name)

    if (scope.isDefined)
      map = map ++ Map("scope" -> scope.get)
    if (nested.isDefined)
      map = map ++ Map("nested" -> nested.get)
    if (isGlobal.isDefined)
      map = map ++ Map("global" -> isGlobal.get)
    if (facetFilter.isDefined)
      map = map ++ Map("facet_filter" -> facetFilter.get)


    if (field.isDefined)
      map = map ++ Map("field" -> field)
    else if (keyField.isDefined) {
      map = map ++ Map("key_field" -> keyField)
      if (valueField.isDefined)
        map = map ++ Map("value_field" -> valueField)
      else
        throw new IllegalArgumentException("Invalid key_field: value_field required");
    }
    else if (keyScript.isDefined) {
      map = map ++ Map("key_script" -> keyScript)
      if (valueScript.isDefined)
        map = map ++ Map("value_script" -> valueField) ++ Map("params" -> decompose(params)(base_formats))
      else
        throw new IllegalArgumentException("Invalid key_script: value_script required");
    }
    Map(internalName -> map)
  }
}


case class RangeFacet(field: Option[String] = None, ranges: List[RangeForFacet] = Nil, keyField: Option[String] = None,
                      valueField: Option[String] = None, keyScript: Option[String], valueScript: Option[String] = None,
                      params: Map[String, Any] = Map.empty[String, Any],
                      var scope: Option[String] = None, var nested: Option[Boolean] = None,
                      var isGlobal: Option[Boolean] = None, var facetFilter: Option[Filter] = None) extends Facet {

  def internalName = "range"

  implicit val base_formats = DefaultFormats ++ JodaTimeSerializers.all


  override protected def serialize: JObject = {

    var json = super.serialize
    if (!ranges.isEmpty) {
      json ~= ("ranges" -> decompose(ranges))
    }
    if (field.isDefined)
      json ~= ("field" -> field)
    else if (keyField.isDefined) {
      json ~= ("key_field" -> keyField)
      if (valueField.isDefined)
        json ~= ("value_field" -> valueField)
      else
        throw new IllegalArgumentException("Invalid key_field: value_field required");
    }
    else if (keyScript.isDefined) {
      json ~= ("key_script" -> keyScript)
      if (valueScript.isDefined)
        json ~= ("value_script" -> valueField) ~ ("params" -> decompose(params)(base_formats))
      else
        throw new IllegalArgumentException("Invalid key_script: value_script required");
    }

    json
  }


  override def toMap: Map[String, Any] = {

    var map: Map[String, Any] = Map.empty[String, Any]

    if (scope.isDefined)
      map = map ++ Map("scope" -> scope.get)
    if (nested.isDefined)
      map = map ++ Map("nested" -> nested.get)
    if (isGlobal.isDefined)
      map = map ++ Map("global" -> isGlobal.get)
    if (facetFilter.isDefined)
      map = map ++ Map("facet_filter" -> facetFilter.get)


    if (!ranges.isEmpty) {
      map = map ++ Map("ranges" -> decompose(ranges))
    }
    if (field.isDefined)
      map = map ++ Map("field" -> field)
    else if (keyField.isDefined) {
      map = map ++ Map("key_field" -> keyField)
      if (valueField.isDefined)
        map = map ++ Map("value_field" -> valueField)
      else
        throw new IllegalArgumentException("Invalid key_field: value_field required");
    }
    else if (keyScript.isDefined) {
      map = map ++ Map("key_script" -> keyScript)
      if (valueScript.isDefined)
        map = map ++ Map("value_script" -> valueField) ++ Map("params" -> params)
      else
        throw new IllegalArgumentException("Invalid key_script: value_script required");
    }
    Map(internalName -> map)
  }

}

object TermFacet {
  def apply(field:String)=new TermFacet(field=Some(field))
  def apply(field:String, size:Int)=new TermFacet(field=Some(field), size=Some(size))
}

case class TermFacet(field: Option[String] = None, fields: List[String] = Nil, size: Option[Int] = Some(10), order: Option[TermFacetOrder] = None,
                     exclude: List[String] = Nil, regex: Option[String] = None, regexFlags: Option[String] = Some("DOTALL"),
                     scriptField: Option[String] = None, allTerms: Option[Boolean] = None,
                     var scope: Option[String] = None, var nested: Option[Boolean] = None,
                     var isGlobal: Option[Boolean] = None, var facetFilter: Option[Filter] = None) extends Facet {

  def internalName = "terms"

  implicit val base_formats = DefaultFormats ++ JodaTimeSerializers.all

  override protected def serialize: JObject = {

    var json = super.serialize ~ ("size" -> size) ~ ("script" -> scriptField) ~  ("all_terms" -> allTerms)
    if (!fields.isEmpty)
      json ~= ("fields" -> fields)
    else if (field.isDefined)
      json ~= ("field" -> field)
    if (order.isDefined)
      json ~=  ("order" -> order.get.name)
    if (!exclude.isEmpty)
      json ~= ("exclude" -> exclude)

    if (regex.isDefined && (field.isDefined || !fields.isEmpty))
      json ~= ("regex" -> regex) ~ ("regex_flags" -> regexFlags)

    json
  }

  override def toMap: Map[String, Any] = {

    var map: Map[String, Any] = Map("size" -> size) ++ Map("script" -> scriptField) ++ Map("order" -> order.get.name) ++ Map("exclude" -> exclude) ++ Map("all_terms" -> allTerms)

    if (scope.isDefined)
      map = map ++ Map("scope" -> scope.get)
    if (nested.isDefined)
      map = map ++ Map("nested" -> nested.get)
    if (isGlobal.isDefined)
      map = map ++ Map("global" -> isGlobal.get)
    if (facetFilter.isDefined)
      map = map ++ Map("facet_filter" -> facetFilter.get)


    if (!fields.isEmpty)
      map = map ++ Map("fields" -> fields)
    else if (field.isDefined)
      map = map ++ Map("field" -> field)

    if (regex.isDefined && (field.isDefined || !fields.isEmpty))
      map = map ++ Map("regex" -> regex) ++ Map("regex_flags" -> regexFlags)

    Map(internalName -> map)
  }
}

case class TermStatsFacet(size: Option[Int] = Some(10), order: Option[TermStatsFacetOrder] = None,
                          keyField: Option[String] = None,
                          valueField: Option[String] = None, keyScript: Option[String], valueScript: Option[String] = None,
                          params: Map[String, Any] = Map.empty[String, Any],
                          var scope: Option[String] = None, var nested: Option[Boolean] = None,
                          var isGlobal: Option[Boolean] = None, var facetFilter: Option[Filter] = None) extends Facet {

  def internalName = "terms_stats"

  implicit val base_formats = DefaultFormats ++ JodaTimeSerializers.all

  override protected def serialize: JObject = {

    var json = super.serialize ~ ("size" -> size) ~ ("order" -> order.get.name)
    if (keyField.isDefined)
      json ~= ("key_field" -> keyField) ~ ("value_field" -> valueField)
    else if (keyScript.isDefined)
      json ~= ("key_script" -> keyScript) ~ ("value_script" -> valueScript) ~ ("params" -> decompose(params)(base_formats))

    json
  }

  override def toMap: Map[String, Any] = {

    var map: Map[String, Any] = Map("size" -> size) ++ Map("order" -> order.get.name)

    if (scope.isDefined)
      map = map ++ Map("scope" -> scope.get)
    if (nested.isDefined)
      map = map ++ Map("nested" -> nested.get)
    if (isGlobal.isDefined)
      map = map ++ Map("global" -> isGlobal.get)
    if (facetFilter.isDefined)
      map = map ++ Map("facet_filter" -> facetFilter.get)

    if (keyField.isDefined)
      map = map ++ Map("key_field" -> keyField) ++ Map("value_field" -> valueField)
    else if (keyScript.isDefined)
      map = map ++ Map("key_script" -> keyScript) ++ Map("value_script" -> valueScript) ++ Map("params" -> params)

    Map(internalName -> map)

  }
}


case class GeoDistanceFacet(field: String, pin: String, ranges: List[RangeForFacet] = Nil,
                            valueField: Option[String] = None, distanceUnit: Option[DistanceUnit]=None,
                            distanceType: Option[DistanceType]=None, valueScript: Option[String] = None, params: Map[String, Any] = Map.empty[String, Any],
                            var scope: Option[String] = None, var nested: Option[Boolean] = None,
                            var isGlobal: Option[Boolean] = None, var facetFilter: Option[Filter] = None) extends Facet {

  def internalName = "geo_distance"

  implicit val base_formats = DefaultFormats ++ JodaTimeSerializers.all


  override protected def serialize: JObject = {

    var json = super.serialize ~ (field -> pin) ~ ("distance_type" -> distanceType.get.name) ~ ("unit" -> distanceUnit.get.name)
    if (!ranges.isEmpty) {
      json ~= ("ranges" -> decompose(ranges))
    }
    if (valueField.isDefined)
      json ~= ("value_field" -> valueField)
    else if (valueScript.isDefined)
      json ~= ("value_script" -> valueScript) ~ ("params" -> decompose(params)(base_formats))

    json
  }

  override def toMap: Map[String, Any] = {

    var map: Map[String, Any] = Map(field -> pin) ++ Map("distance_type" -> distanceType.get.name) ++ Map("unit" -> distanceUnit.get.name)

    if (scope.isDefined)
      map = map ++ Map("scope" -> scope.get)
    if (nested.isDefined)
      map = map ++ Map("nested" -> nested.get)
    if (isGlobal.isDefined)
      map = map ++ Map("global" -> isGlobal.get)
    if (facetFilter.isDefined)
      map = map ++ Map("facet_filter" -> facetFilter.get)

    if (!ranges.isEmpty) {
      map = map ++ Map("ranges" -> decompose(ranges))
    }
    if (valueField.isDefined)
      map = map ++ Map("value_field" -> valueField)
    else if (valueScript.isDefined)
      map = map ++ Map("value_script" -> valueScript) ++ Map("params" -> params)

    Map(internalName -> map)

  }
}

object StatisticalFacet {
  def apply(field:String)=new StatisticalFacet(Some(field))
}

case class StatisticalFacet(field: Option[String], script: Option[String] = None, params: Map[String, Any] = Map.empty[String, Any],
                            var scope: Option[String] = None, var nested: Option[Boolean] = None,
                            var isGlobal: Option[Boolean] = None, var facetFilter: Option[Filter] = None) extends Facet {

  def internalName = "statistical"

  implicit val base_formats = DefaultFormats ++ JodaTimeSerializers.all


  override protected def serialize: JObject = {

    var json = super.serialize
    if (field.isDefined)
      json ~= ("field" -> field)
    else if (script.isDefined)
      json ~= ("script" -> script) ~ ("params" -> decompose(params)(base_formats))

    json
  }

  override def toMap: Map[String, Any] = {

    var map: Map[String, Any] = Map.empty[String, Any]

    if (scope.isDefined)
      map = map ++ Map("scope" -> scope.get)
    if (nested.isDefined)
      map = map ++ Map("nested" -> nested.get)
    if (isGlobal.isDefined)
      map = map ++ Map("global" -> isGlobal.get)
    if (facetFilter.isDefined)
      map = map ++ Map("facet_filter" -> facetFilter.get)

    if (field.isDefined)
      map = map ++ Map("field" -> field)
    else if (script.isDefined)
      map = map ++ Map("script" -> script) ++ Map("params" -> decompose(params)(base_formats))

    Map(internalName -> map)

  }

}


