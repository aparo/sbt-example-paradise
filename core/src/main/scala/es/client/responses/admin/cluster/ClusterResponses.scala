package es.client.responses.admin.cluster

import es.mapping.DocumentObjectField
import org.joda.time.DateTime
import scala.xml.NodeSeq

/**
 * Created by alberto on 22/11/13.
 */

//ClusterHealth
case class ESClusterHealth(cluster_name: String, status: String, timed_out: Boolean, number_of_nodes: Double,
                           number_of_data_nodes: Double, active_primary_shards: Double, active_shards: Double,
                           relocating_shards: Double, initializing_shards: Double, unassigned_shards: Double) {
  def getTotalShards: Int = (active_shards + relocating_shards + initializing_shards + unassigned_shards).toInt

  def getStatusHtml: NodeSeq = {
    status match {
      case "green" => <span class="label label-success">Green</span>
      case "yellow" => <span class="label label-warning">Yellow</span>
      case _ => <span class="label label-danger">Red</span>
    }
  }
}

//ClusterState
case class ESShard(state: String, primary: Boolean, node: String, shard: Int, index: String)

case class ESShards(shards: Map[Int, List[ESShard]])

case class ESRouting(indices: Map[String, ESShards])

case class ESIndex(state: String, settings: Map[String, String], mappings: Map[String, DocumentObjectField], aliases: List[String])

case class ESMetadata(templates: Option[Map[String, Any]], indices: Map[String, ESIndex])

case class ESNode(name: String, transport_address: String, attributes: Option[Map[String, Any]], hostname: Option[String], timestamp: Option[DateTime], indices: Option[ESIndices])

case class ESBlockStatus(description: String, retryable: Boolean, levels: List[String])

case class ESBlocks(indices:Map[String, Map[String, ESBlockStatus]])

case class ESClusterState(cluster_name: String, master_node: String, blocks: Option[ESBlocks], nodes: Map[String, ESNode], metadata: ESMetadata, routing_table: ESRouting) {
  def getIndexNumber: Int = metadata.indices.size

  def getIndex(index: String): ESIndex = {
    var a = metadata.indices
    a.get(index).get
  }

  def getNode(id: String) = nodes.get(id).get
}

//NodeStats

case class ESPercolate(total: Float, time_in_millis: Float, current: Float, memory_size_in_bytes: Float, memory_size: String)

case class ESIndices(docs: Map[String, Long], store: Map[String, Long], indexing: Map[String, Long], get: Map[String, Long], search: Map[String, Long], percolate: ESPercolate)

case class ESNodeStats(cluster_name: String, nodes: Map[String, ESNode])


// NodeInfo
case class ESVersion(number: String, build_hash: String, build_timestamp: String, build_snapshot: Boolean, lucene_version: String)

case class ESNodeInfo(ok: Boolean, status: Integer, name: String, version: ESVersion, tagline: String)
