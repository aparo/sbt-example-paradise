package es.utils

abstract class DistanceType(val name: String)
object DistanceType {
  case object Arc extends DistanceType("arc")
  case object Plane extends DistanceType("plane")
}


abstract class DistanceUnit(val name: String)
object DistanceUnit {
  case object Km extends DistanceUnit("km")

}

abstract class TermFacetOrder(val name: String)
object TermFacetOrder {
  case object Count extends TermFacetOrder("count")

}

abstract class TermStatsFacetOrder(val name: String)
object TermStatsFacetOrder {
  case object Count extends TermStatsFacetOrder("count")


}