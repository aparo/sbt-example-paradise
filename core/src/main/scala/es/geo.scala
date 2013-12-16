package es

import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

object GeoPoint {
  val LATITUDE: String = "lat"
  val LONGITUDE: String = "lon"
  val GEOHASH: String = "geohash"

  def apply(geohash:String)=GeoHashUtils.decode(geohash)
}

case class GeoPoint(var lat: Double=0.0, var lon: Double=0.0) {

  def resetFromString(value: String): GeoPoint = {
    val comma: Int = value.indexOf(',')
    if (comma != -1) {
//      return GeoPoint(Double.parseDouble(value.substring(0, comma).trim),
      return GeoPoint((value.substring(0, comma).trim).toDouble, ((value.substring(comma + 1).trim).toDouble))

    }
    resetFromGeoHash(value)

  }

  def resetFromGeoHash(hash: String): GeoPoint = GeoHashUtils.decode(hash)

  def geohash: String = GeoHashUtils.encode(lat, lon)

  def getGeohash: String = GeoHashUtils.encode(lat, lon)

  def toJson:JObject=("lat" -> lat) ~ ("lon" -> lon)
}

