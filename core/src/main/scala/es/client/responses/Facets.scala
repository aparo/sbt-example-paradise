package es.client.responses

import org.joda.time.DateTime

package object facet{
  type Facets=Map[String,Facet]
  val EmptyFacets=Map.empty[String,Facet]
}

sealed class Facet

//{"_type":"terms","missing":0,"total":3,"other":0,"terms":[{"term":"foo","count":2},{"term":"bar","count":1}]}}
case class Term(term:Any, count:Long=0)

case class TermsFacet(missing:Long=0, total:Long=0, other:Long=0, terms:List[Term]=Nil) extends Facet

case class DateHistogramEntry(time:DateTime, count:Long, totalCount:Option[Long], min:Option[Double], max:Option[Double], total:Option[Double], mean:Option[Double])

case class DateHistogramFacet(entries:List[DateHistogramEntry]=Nil)  extends Facet

case class HistogramEntry(key:Any, count:Long)

case class HistogramFacet(entries:List[HistogramEntry]=Nil)  extends Facet

case class TermStat(term:String, count:Long, total_count:Long, min:Double, max:Double, total:Double, mean:Double)

case class TermsStatsFacet(missing:Long, terms:List[TermStat]=Nil)  extends Facet

case class StatisticalFacet(count:Long, total:Double, min:Double, max:Double, mean:Double,
    sum_of_squares:Double, variance:Double, std_deviation:Double) extends Facet


case class Range(from:Option[Any]=None, to:Option[Any]=None, count:Long, totalCount:Long, min:Double, max:Double, total:Double, mean:Double)

case class RangeFacet(ranges:List[Range]=Nil) extends Facet


case class FilterFacet(count:Long) extends Facet
