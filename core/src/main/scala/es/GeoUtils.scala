/* Copyright 2009-2013 - The Net Planet Europe S.R.L.  All Rights Reserved. */
package es

/**
  */
object GeoUtils {

  val MAX_LEVELS_POSSIBLE = 50
  val SIZE = 64

  /**
   * Calculate the width (in meters) of geohash cells at a specific level
   * @param level geohash level must be greater or equal to zero
   * @return the width of cells at level in meters
   */
  def geoHashCellWidth(level: Int): Double = {
    assert(level >= 0)
    return EARTH_EQUATOR / (1L << ((((level + 1) / 2) * 3) + ((level / 2) * 2)))
  }

  /**
   * Calculate the width (in meters) of quadtree cells at a specific level
   * @param level quadtree level must be greater or equal to zero
   * @return the width of cells at level in meters
   */
  def quadTreeCellWidth(level: Int): Double = {
    assert(level >= 0)
    return EARTH_EQUATOR / (1L << level)
  }

  /**
   * Calculate the height (in meters) of geohash cells at a specific level
   * @param level geohash level must be greater or equal to zero
   * @return the height of cells at level in meters
   */
  def geoHashCellHeight(level: Int): Double = {
    assert(level >= 0)
    return EARTH_POLAR_DISTANCE / (1L << ((((level + 1) / 2) * 2) + ((level / 2) * 3)))
  }

  /**
   * Calculate the height (in meters) of quadtree cells at a specific level
   * @param level quadtree level must be greater or equal to zero
   * @return the height of cells at level in meters
   */
  def quadTreeCellHeight(level: Int): Double = {
    assert(level >= 0)
    return EARTH_POLAR_DISTANCE / (1L << level)
  }

  /**
   * Calculate the size (in meters) of geohash cells at a specific level
   * @param level geohash level must be greater or equal to zero
   * @return the size of cells at level in meters
   */
  def geoHashCellSize(level: Int): Double = {
    assert(level >= 0)
    val w: Double = geoHashCellWidth(level)
    val h: Double = geoHashCellHeight(level)
    return Math.sqrt(w * w + h * h)
  }

  /**
   * Calculate the size (in meters) of quadtree cells at a specific level
   * @param level quadtree level must be greater or equal to zero
   * @return the size of cells at level in meters
   */
  def quadTreeCellSize(level: Int): Double = {
    assert(level >= 0)
    return Math.sqrt(EARTH_POLAR_DISTANCE * EARTH_POLAR_DISTANCE + EARTH_EQUATOR * EARTH_EQUATOR) / (1L << level)
  }

  /**
   * Normalize longitude to lie within the -180 (exclusive) to 180 (inclusive) range.
   *
   * @param lon Longitude to normalize
   * @return The normalized longitude.
   */
  def normalizeLon(lon: Double): Double = centeredModulus(lon, 360)


  /**
   * Normalize latitude to lie within the -90 to 90 (both inclusive) range.
   * <p/>
   * Note: You should not normalize longitude and latitude separately,
   * because when normalizing latitude it may be necessary to
   * add a shift of 180&deg; in the longitude.
   * For this purpose, you should call the
   * {@link #normalizePoint(GeoPoint)} function.
   *
   * @param lat Latitude to normalize
   * @return The normalized latitude.
   * @see #normalizePoint(GeoPoint)
   */
  def normalizeLat(lat: Double): Double = {
    var l = centeredModulus(lat, 360)
    if (l < -90) {
      l = -180 - l
    }
    else if (lat > 90) {
      l = 180 - l
    }
    l
  }

  /**
   * Normalize the geo {@code Point} for the given coordinates to lie within
   * their respective normalized ranges.
   * <p/>
   * You can control which coordinate gets normalized with the two flags.
   * <p/>
   * Note: A shift of 180&deg; is applied in the longitude if necessary,
   * in order to normalize properly the latitude.
   * If normalizing latitude but not longitude, it is assumed that
   * the longitude is in the form x+k*360, with x in ]-180;180],
   * and k is meaningful to the application.
   * Therefore x will be adjusted while keeping k preserved.
   *
   * @param point   The point to normalize in-place.
   * @param normLat Whether to normalize latitude or leave it as is.
   * @param normLon Whether to normalize longitude.
   */
  def normalizePoint(point: GeoPoint, normLatitude: Boolean=true, normLongituge: Boolean=true):GeoPoint= {
    var lat: Double = point.lat
    var lon: Double = point.lon
    val normLat = normLatitude && (lat > 90 || lat <= -90)
    val normLon = normLongituge && (lon > 180 || lon <= -180)
    if (normLat) {
      lat = centeredModulus(lat, 360)
      var shift: Boolean = true
      if (lat < -90) {
        lat = -180 - lat
      }
      else if (lat > 90) {
        lat = 180 - lat
      }
      else {
        shift = false
      }
      if (shift) {
        if (normLon) {
          lon += 180
        }
        else {
          lon += (if (normalizeLon(lon) > 0) -180 else 180)
        }
      }
    }
    if (normLon) {
      lon = centeredModulus(lon, 360)
    }
    GeoPoint(lat, lon)
  }

  private def centeredModulus(dividend: Double, divisor: Double): Double = {
    var rtn: Double = dividend % divisor
    if (rtn <= 0) {
      rtn += divisor
    }
    if (rtn > divisor / 2) {
      rtn -= divisor
    }
    rtn
  }

  /** Earth ellipsoid major axis defined by WGS 84 in meters */
  final val EARTH_SEMI_MAJOR_AXIS: Double = 6378137.0
  /** Earth ellipsoid minor axis defined by WGS 84 in meters */
  final val EARTH_SEMI_MINOR_AXIS: Double = 6356752.314245
  /** Earth ellipsoid equator length in meters */
  final val EARTH_EQUATOR: Double = 2 * Math.PI * EARTH_SEMI_MAJOR_AXIS
  /** Earth ellipsoid polar distance in meters */
  final val EARTH_POLAR_DISTANCE: Double = Math.PI * EARTH_SEMI_MINOR_AXIS
}