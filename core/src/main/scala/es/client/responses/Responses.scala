package es.client.responses

import net.liftweb.json.JsonAST.JValue
import es.client.responses.facet.Facets
import scala.xml.NodeSeq

case class DeleteResponse(ok: Boolean = true, _index: String = "NA", _type: String = "NA", _id: String = "NA", found: Boolean = true)

case class GetResponse(index: String, docType: String, id: String, source: JValue, version: Long = -1,
                       exists: Boolean = false)

case class CountResponse(count: Long = 0, _shards: Shards)

case class Shards(total: Int = 0, successful: Int = 0, failed: Int = 0)

case class Routing(state: String, primary: Boolean, node: String, shard: Int, index: String)

case class Shard(routing: Routing, state: String, index: Index, translog: Translog, docs: Docs, merges: Marges, refresh: Refresh, flush: Flush)

case class Flush(total: Long, total_time_in_millis: Long)

case class Refresh(total: Long, total_time_in_millis: Long)

case class Marges(current: Long, current_docs: Long, current_size_in_bytes: Long, total: Long, total_time_in_millis: Int, total_docs: Long, total_size_in_bytes: Long)

case class Docs(num_docs: Long, max_doc: Long, deleted_docs: Long)

case class Translog(id: Option[Long], operations: Option[Long])

case class Index(primary_size_in_bytes: Option[Long], size_in_bytes: Long)

case class Indices(index: Index, translog: Translog, docs: Docs, merges: Marges, refresh: Refresh, flush: Flush, shards: Map[String, List[Shard]]) {
  def getNumberOfShards = shards.size

  def getNumberOfNodes = shards.map(s => s._2.length).max

  def getStateHtml(): NodeSeq = {
    val nodes = this.getNumberOfNodes
    if (nodes > 1) {
      val status = shards.flatMap {
        l =>
          if (l._2.size == nodes)
            l._2.map(s => s.state)
          else
            "Yellow"
      }.toSet
//      if (nodes == 1 || nodes != index_replicas)
//        <span class="label label-warning">Yellow</span>
//      else
//        <span class="label label-success">Green</span>
          if (status.size == 1 && status.contains("STARTED"))
            <span class="label label-success">Green</span>
          else
           <span class="label label-warning">Yellow</span>
    }
    else
      <span class="label label-warning">Yellow</span>
  }


}


case class IndexResponse(ok: Boolean, _index: String, _type: String, _id: String, _version: Long = 0)


case class IndexShardResult(_shards: Shards)

case class DeleteByQueryResult(ok: Boolean, _indices: Map[String, IndexShardResult])

case class HitResponse(index: String, docType: String, id: String, score: Option[Double] = None,
                       version: Option[Long] = None, source: JValue, sort: List[Any] = Nil,
                       fields: Map[String, Any] = Map.empty[String, Any])

case class Hits(total: Long, max_score: Option[Double] = None, hits: List[HitResponse] = Nil) {
  def maxScore = max_score
}

case class SearchResponse(took: Double, timedOut: Boolean, shards: Shards, hits: Hits,
                          facets: Facets = facet.EmptyFacets,
                           scrollId:Option[String]=None) {
  def getHit(pos: Int): HitResponse = hits.hits(pos)
}

case class ErrorResponse(error: String, status: Int = 0)
