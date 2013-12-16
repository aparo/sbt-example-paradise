package es.search

import es.GeoPoint

package object sort {
  type Sort = Map[String, SortParameter]
  val EmptySort = Map.empty[String, SortParameter]
}

object SortParameter {
  def apply(order: Boolean): SimpleSort = {
    val orderName = order match {
      case true => "asc"
      case false => "desc"
    }
    SimpleSort(orderName)
  }

  def apply(orderName: String): SimpleSort = {
    val ordering = orderName.charAt(0) match {
      case '+' => "asc"
      case '-' => "desc"
      case _ => "asc"
    }
    SimpleSort(ordering)
  }
}

sealed class SortParameter

case class SimpleSort(order: String = "asc", ignore_unmapped: Boolean = true,
                      missing: Option[Any] = None) extends SortParameter

case class ScriptSort(script: String, `type`: String = "number", order: String = "asc", ignore_unmapped: Boolean = true,
                      missing: Option[Any] = None, params: Map[String, Any] = Map.empty[String, Any],
                      language: String = "mvel"
                       ) extends SortParameter

//TODO add custom serializer to manage field
case class GeoDistanceSort(field: String, geopoint: GeoPoint, order: String = "asc", ignore_unmapped: Boolean = true,
                           missing: Option[Any] = None, unit: String = "km"
                            ) extends SortParameter


//case class Sort(params: List[SortParameter]=Nil)

