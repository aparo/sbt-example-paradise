package es.utils

/**
 * Created with IntelliJ IDEA.
 * User: ivan
 * Date: 25/09/13
 * Time: 11:39
 * To change this template use File | Settings | File Templates.
 */
abstract class OrderType(val name: String)
object OrderType {
  case object Desc extends OrderType("desc")
  case object Asc extends OrderType("asc")
}