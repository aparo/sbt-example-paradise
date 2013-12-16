package es.utils

/**
 * Created with IntelliJ IDEA.
 * User: ivan
 * Date: 25/09/13
 * Time: 11:26
 * To change this template use File | Settings | File Templates.
 */
abstract class DateKind(val name: String)
object DateKind {
  case object Month extends DateKind("month")
  case object Year extends DateKind("year")
  case object Day extends DateKind("day")
  case object Week extends DateKind("week")
  case object Hour extends DateKind("hour")
  case object Minute extends DateKind("minute")
  case object Second extends DateKind("second")
  case object OneYear extends DateKind("1y")
  case object OneMonth extends DateKind("1m")
  case object OneWeek extends DateKind("1w")
  case object OneDay extends DateKind("1d")
  case object OneHour extends DateKind("1h")
  case object OneMinute extends DateKind("1m")
  case object OneSecond extends DateKind("1s")
  case object Quarter extends DateKind("quarter")

}