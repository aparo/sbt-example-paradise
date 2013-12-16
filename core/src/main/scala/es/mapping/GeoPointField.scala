package es.mapping

class GeoPointField extends AbstractField {
  var null_value: Option[Any] = None
  var include_in_all: Option[Boolean] = None
  var lat_lon: Option[Boolean] = None
  var geohash: Option[Boolean] = None
  var geohash_precision: Option[Int] = None
  var normalize_lon: Option[Boolean] = None
  var normalize_lat: Option[Boolean] = None
  var validate_lon: Option[Boolean] = None
  var validate_lat: Option[Boolean] = None

  override val `type`: String = "geo_point"

  override def read(in: Map[String, Any]) {
    super.read(in)
    if (in.contains("null_value"))
      null_value = Some(in("null_value"))
    if (in.contains("include_in_all"))
      include_in_all = Some(in("include_in_all").asInstanceOf[Boolean])
    if (in.contains("lat_lon"))
      lat_lon = Some(in("lat_lon").asInstanceOf[Boolean])
    if (in.contains("geohash"))
      geohash = Some(in("geohash").asInstanceOf[Boolean])
    if (in.contains("geohash_precision"))
      geohash_precision = Some(in("geohash_precision").asInstanceOf[Int])
    if (in.contains("normalize_lon"))
      normalize_lon = Some(in("normalize_lon").asInstanceOf[Boolean])
    if (in.contains("normalize_lat"))
      normalize_lat = Some(in("normalize_lat").asInstanceOf[Boolean])
    if (in.contains("validate_lon"))
      validate_lon = Some(in("validate_lon").asInstanceOf[Boolean])
    if (in.contains("validate_lat"))
      validate_lat = Some(in("validate_lat").asInstanceOf[Boolean])
  }
}

