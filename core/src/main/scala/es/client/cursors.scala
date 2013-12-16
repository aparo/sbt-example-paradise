package es.client

import es.client.responses.{SearchResponse, HitResponse}
import org.joda.time.DateTime
import es.DefaultESIdentifier
import es.client.responses.facet._
import scala.Some
import es.search.Search

class EmptyESCursor[BaseDocument](cursor: ESBaseCursor, builder: HitResponse => BaseDocument) extends ESCursor[BaseDocument](cursor, builder) {

  override def hasNext: Boolean = false

  override def next(): BaseDocument = builder(cursor.next())

  override def total: Long = 0L

  override def maxScore: Option[Double] = Some(0)

  override def facets=EmptyFacets

}


class ESCursor[BaseDocument](cursor: ESBaseCursor, builder: HitResponse => BaseDocument) extends Iterator[BaseDocument] {

  def hasNext: Boolean = cursor.hasNext

  def next(): BaseDocument = builder(cursor.next())

  def total: Long = cursor.total

  def maxScore: Option[Double] = cursor.maxScore

  def facets=cursor.facets
}

class ESCursorMap(cursor: ESBaseCursor, field: String) extends Iterator[Map[String, Any]] {

  def hasNext: Boolean = cursor.hasNext

  def next(): Map[String, Any] = {
    cursor.next().fields
  }

  def total: Long = cursor.total

  def maxScore: Option[Double] = cursor.maxScore

}

class ESCursorField[T](cursor: ESBaseCursor, field: String) extends Iterator[T] {

  def hasNext: Boolean = cursor.hasNext

  def next(): T = {
    cursor.next().fields.get(field).get.asInstanceOf[T]
  }

  def total: Long = cursor.total

  def maxScore: Option[Double] = cursor.maxScore
}

class ESListCursor(cursor: ESBaseCursor, field: String) extends Iterator[DateTime] {

  def hasNext: Boolean = cursor.hasNext

  def next() = {

    implicit val formats = DefaultESIdentifier.formats
    val fields = cursor.next().fields
    fields.get(field).get.asInstanceOf[DateTime]
  }

  def total: Long = cursor.total

  def maxScore: Option[Double] = cursor.maxScore
}


trait ESBaseCursor extends Iterator[HitResponse] {
  def total: Long

  def maxScore: Option[Double]

  def facets: Facets

}

class EmptyESBaseCursor extends ESBaseCursor {

  def hasNext: Boolean = false

  def next(): HitResponse = null

  def total: Long = 0L

  def maxScore = Some(0.0)

  def facets: Facets = EmptyFacets
}

class NativeCursor(client: Client, search: Search, indices: Seq[String], docTypes: Seq[String],
                   params: Map[String, String] = Map.empty[String, String]) extends ESBaseCursor {

  private val validatedSearch: Search = search

  private var position: Int = 0
  private var response: Option[SearchResponse] = None
  private var curr_start = search.from
  private var next_start = search.from
  private lazy val searchSize = getSearchSize
  private var scrollId:Option[String]=None

  private def getSearchSize: Int = {
    var res = search.size
    if (res == -1) {
      if (search.bulkRead != -1) {
        res = search.bulkRead
      } else {
        res = 10
      }

    }
    res.toInt
  }


  def hasNext: Boolean = {
    if (response == null) {
      doQuery()
    }
    //println(s"hasNext $position $total")
    if (position < total) {
      //check searchsize
      if (search.size != -1) {
        return position < searchSize
      }
      return true
    }

    false
  }

  def next(): HitResponse = {
    if (!response.isDefined) {
      doQuery()
    }
    if (position < curr_start + searchSize) {
      val res = response.get.getHit(position % searchSize)
      position += 1
      return res
    }
    doQuery()
    val res = response.get.getHit(position % searchSize)
    position += 1
    res
  }

  def maxScore: Option[Double] = {
    if (response == null) {
      doQuery()
    }
    response.get.hits.max_score
  }

  def total: Long = {
    if (!response.isDefined) {
      doQuery()
    }
    response.get.hits.total
  }


  def doQuery() {
    if(!response.isDefined){
      curr_start = next_start
      if(validatedSearch.isScan){
        val newSearch = validatedSearch.copy(from = curr_start, size = getSearchSize)
        val scrollResult = newSearch.getResponse(client, indices = indices, docTypes = docTypes, params = newSearch.getQueryParameters ++ params)
        scrollId=scrollResult.scrollId
      }
    }
    if(scrollId.isDefined){
      response=Some(client.scroll(scrollId.get, validatedSearch.scrollTime))
      scrollId=response.get.scrollId

    }else{
      val newSearch = search.copy(from = curr_start, size = getSearchSize)
      response = Some(newSearch.getResponse(client, indices, docTypes, params))
      scrollId=response.get.scrollId
    }
    next_start += searchSize
  }

  def facets: Facets = {
    if (!response.isDefined) {
      doQuery()
    }
    response.get.facets
  }

}