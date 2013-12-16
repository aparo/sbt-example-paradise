package es.client

import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import es.client.responses.admin.cluster._
import es.DefaultESIdentifier

case class ClusterManager(client: Client) {
  implicit val formats = DefaultESIdentifier.formats

  def health(indices: Seq[String] = Nil, level: String = "cluster", wait_for_status: Option[String] = None,
             wait_for_relocating_shards: Option[Int] = None, timeout: Int = 30): ESClusterHealth = {
    /*
          Check the current :ref:`cluster health <es-guide-reference-api-admin-cluster-health>`.
          Request Parameters

          The cluster health API accepts the following request parameters:

          :param level: Can be one of cluster, indices or shards. Controls the
                          details level of the health information returned.
                          Defaults to *cluster*.
          :param wait_for_status: One of green, yellow or red. Will wait (until
                                  the timeout provided) until the status of the
                                  cluster changes to the one provided.
                                  By default, will not wait for any status.
          :param wait_for_relocating_shards: A number controlling to how many
                                             relocating shards to wait for.
                                             Usually will be 0 to indicate to
                                             wait till all relocation have
                                             happened. Defaults to not to wait.
          :param timeout: A time based parameter controlling how long to wait
                          if one of the wait_for_XXX are provided.
                          Defaults to 30s.
    */
    val path = client.makeUrl(indices, path = "_cluster/health")

    val json = ("level" -> level) ~ ("wait_for_status" -> wait_for_status) ~ ("timeout" -> s"${timeout}s") ~
      ("wait_for_relocating_shards" -> wait_for_relocating_shards)

    val list = Set("cluster", "indices", "shards")
    if (level != "cluster") {

      if (!list.contains(level))
        throw new ValueError("Invalid level: " + level)
    }
    val l2 = Set("green", "yellow", "red")
    if (wait_for_status.isDefined) {

      if (!l2.contains(wait_for_status.get))
        throw new ValueError("Invalid wait_for_status: " + wait_for_status)
    }

    client.doCall("GET", path, json).extract[ESClusterHealth]

  }


  def nodes_info(nodes: List[String] = Nil): ESNodeInfo = {
    /*
          The cluster :ref:`nodes info <es-guide-reference-api-admin-cluster-state>` API allows to retrieve one or more (or all) of
          the cluster nodes information.
    */
    val nodeList = nodes.mkString(",")

    val path = if (!nodeList.isEmpty) client.makeUrl(path = List("_cluster", "nodes", nodeList).mkString("/")) else client.makeUrl(path = List("_cluster", "nodes").mkString("/"))

    client.doCall("GET", path).extract[ESNodeInfo]

  }


  def node_stats(nodes: List[String] = Nil): ESNodeStats = {
    /*
          The cluster :ref:`nodes info <es-guide-reference-api-admin-cluster-nodes-stats>` API allows to retrieve one or more (or all) of
          the cluster nodes information.
    */

    val nodeList = nodes.mkString(",")

    val path = if (!nodeList.isEmpty) client.makeUrl(path = List("_cluster", "nodes", nodeList, "stats").mkString("/")) else client.makeUrl(path = List("_cluster", "nodes", "stats").mkString("/"))
    client.doCall("GET", path).extract[ESNodeStats]

  }

  def state(filter_nodes: Boolean = false, filter_routing_table: Boolean = false, filter_metadata: Boolean = false,
            filter_blocks: Boolean = false, filter_indices: List[String] = Nil): ESClusterState = {

    /*
       Retrieve the :ref:`cluster state <es-guide-reference-api-admin-cluster-state>`.

      :param filter_nodes: set to **true** to filter out the **nodes** part
                           of the response.
      :param filter_routing_table: set to **true** to filter out the
                                   **routing_table** part of the response.
      :param filter_metadata: set to **true** to filter out the **metadata**
                              part of the response.
      :param filter_blocks: set to **true** to filter out the **blocks**
                            part of the response.
      :param filter_indices: when not filtering metadata, a comma separated
                             list of indices to include in the response.

    */

    val list: List[String] = List("_cluster", "state")
    val path = list.mkString("/")
    val indices = filter_indices.mkString(",")

    val json = ("filter_nodes" -> filter_nodes) ~
               ("filter_routing_table" -> filter_routing_table) ~
               ("filter_metadata" , filter_metadata)  ~
               ( "filter_blocks" , filter_blocks ) ~
               ( "filter_indices" , indices )


    val a = client.doCall("GET", path, json).extract[ESClusterState]
    a

  }


}

