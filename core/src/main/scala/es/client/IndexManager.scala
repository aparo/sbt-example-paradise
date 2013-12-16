/*
 *  Copyright (c) 2009-2013 - The Net Planet Europe S.R.L.  All Rights Reserved.
 */

package es.client

import net.liftweb.json.JsonDSL._
import scala.Predef._
import net.liftweb.json.JsonAST._
import responses.admin.index._
import es.DefaultESIdentifier
import es.exceptions.NotFound
import es.mapping.DocumentObjectField

/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 21/02/13
 * Time: 11:07
 */

object Settings{
  def TNPBase:Settings={
    var settings = new Settings(options=Map(

    "index.analysis.analyzer.keyword_lowercase.tokenizer"-> "keyword",
    "index.analysis.analyzer.keyword_lowercase.filter"-> List("lowercase"),

    "index.analysis.analyzer.biword.tokenizer"-> "standard",
    "index.analysis.analyzer.biword.filter"-> List("shingler", "lowercase"),
    "index.analysis.filter.shingler.type"-> "shingle",
    "index.analysis.filter.shingler.min_shingle_size"-> 2,
    "index.analysis.filter.shingler.max_shingle_size"-> 3,

    "index.analysis.analyzer.reverse.tokenizer"-> "standard",
    "index.analysis.analyzer.reverse.filter"-> List("lowercase", "reverse"),
    "index.analysis.analyzer.tnp_standard_lower.tokenizer"-> "standard",
    "index.analysis.analyzer.tnp_standard_lower.filter"-> "lowercase",
    "index.analysis.analyzer.bigram.tokenizer"-> "standard",
    "index.analysis.analyzer.bigram.filter"-> List("tnp_shingler2", "lowercase"),
    "index.analysis.filter.tnp_shingler2.type"-> "shingle",
    "index.analysis.filter.tnp_shingler2.output_unigrams"-> false,
    "index.analysis.filter.tnp_shingler2.min_shingle_size"-> 2,
    "index.analysis.filter.tnp_shingler2.max_shingle_size"-> 2,
    "index.analysis.analyzer.trigram.tokenizer"-> "standard",
    "index.analysis.analyzer.trigram.filter"-> List("tnp_shingler3", "lowercase"),
    "index.analysis.filter.tnp_shingler3.type"-> "shingle",
    "index.analysis.filter.tnp_shingler3.output_unigrams"-> false,
    "index.analysis.filter.tnp_shingler3.min_shingle_size"-> 3,
    "index.analysis.filter.tnp_shingler3.max_shingle_size"-> 3,
    "index.analysis.analyzer.quadrigram.tokenizer"-> "standard",
    "index.analysis.analyzer.quadrigram.filter"-> List("tnp_shingler4", "lowercase"),
    "index.analysis.filter.tnp_shingler4.type"-> "shingle",
    "index.analysis.filter.tnp_shingler4.output_unigrams"-> false,
    "index.analysis.filter.tnp_shingler4.min_shingle_size"-> 4,
    "index.analysis.filter.tnp_shingler4.max_shingle_size"-> 4,
    "index.analysis.analyzer.gram.tokenizer"-> "standard",
    "index.analysis.analyzer.gram.filter"-> List("tnp_shingler24", "lowercase"),
    "index.analysis.filter.tnp_shingler24.type"-> "shingle",
    "index.analysis.filter.tnp_shingler24.output_unigrams"-> false,
    "index.analysis.filter.tnp_shingler24.min_shingle_size"-> 2,
    "index.analysis.filter.tnp_shingler24.max_shingle_size"-> 4
    ))
    settings
  }
}

case class Settings(replicas:Int=1, shards:Int=5, options:Map[String, Any]=Map.empty[String, Any])

case class AliasCommand(command: String, index: String, alias: String)

class IndexAlreadyExistsException(message: String, cause: Throwable)
  extends RuntimeException(message) {
  if (cause != null)
    initCause(cause)

  def this(message: String) = this(message, null)
}

class ValueError(message: String, cause: Throwable)
  extends RuntimeException(message) {
  if (cause != null)
    initCause(cause)

  def this(message: String) = this(message, null)
}

case class IndexManager(client: Client) {

  implicit val formats = DefaultESIdentifier.formats

  /*
        Refresh one or more indices
        If a bulk is full, it sends it.
        (See :ref:`es-guide-reference-api-admin-indices-refresh`)

        :keyword indices: an index or a list of indices
        :keyword timeSleep: seconds to wait

  */

  def refresh(index:String): IndexRefreshResult = refresh(List(index))

  def refresh(indices: Seq[String] = Nil, timeSleep: Int = 0): IndexRefreshResult = {
    //self.conn.force_bulk()
    //indices = self.conn.validate_indices(indices)
    val path = client.makeUrl(indices, path = "_refresh")
    client.doCall("POST", path).extract[IndexRefreshResult]
    //if timeSleep:
    //  time.sleep(timeSleep)
    //self.conn.cluster.health(wait_for_status='green')
    //return result

  }

  def flush(indices: Seq[String] = Nil, refresh: Boolean = true): IndexFlushResult = {
    /*
          Flushes one or more indices (clear memory)
          If a bulk is full, it sends it.

          (See :ref:`es-guide-reference-api-admin-indices-flush`)


          :keyword indices: an index or a list of indices
          :keyword refresh: set the refresh parameter

    */
    val path = client.makeUrl(indices, path = "_flush")

    client.doCall("POST", path, queryArgs = Map("refresh" -> refresh.toString)).extract[IndexFlushResult]
  }


  def changeAliases(commands: Seq[AliasCommand]): IndexAliasResult = {
    /*
          Change the aliases stored.
          (See :ref:`es-guide-reference-api-admin-indices-aliases`)

          :param commands: is a list of 3-tuples; (command, index, alias), where
                           `command` is one of "add" or "remove", and `index` and
                           `alias` are the index and alias to add or remove.

    */

    val body = JObject(List(JField("actions", (commands.map {
      cmd => JObject(List(JField(cmd.command, (
        ("index" -> cmd.index) ~ ("alias" -> cmd.alias))
      )))
    }
      )
    )))

    client.doCall("POST", "_aliases", body).extract[IndexAliasResult]

  }

  def addAlias(alias: String, indices: List[String]): IndexAliasResult = {
    /*
          Add an alias to point to a set of indices.
          (See :ref:`es-guide-reference-api-admin-indices-aliases`)

          :param alias: the name of an alias
          :param indices: a list of indices

    */
    val commands = for (i <- indices) yield AliasCommand(command = "add", index = i, alias = alias)
    changeAliases(commands)

  }

  def deleteAlias(alias: String, indices: Seq[String]): IndexAliasResult = {
    /*
          Delete an alias.
          (See :ref:`es-guide-reference-api-admin-indices-aliases`)

          The specified index or indices are deleted from the alias, if they are
          in it to start with.  This won't report an error even if the indices
          aren't present in the alias.

          :param alias: the name of an alias
          :param indices: a list of indices
    */
    val commands = for (i <- indices) yield AliasCommand(command = "remove", index = i, alias = alias)
    changeAliases(commands)

  }

  def stats(indices: Seq[String] = Nil): IndexStatsResult = {
    /*
          Retrieve the statistic of one or more indices
          (See :ref:`es-guide-reference-api-admin-indices-stats`)

          :keyword indices: an index or a list of indices
    */

    val path = client.makeUrl(indices, path = "_stats")
    client.doCall("GET", path).extract[IndexStatsResult]

  }

  def status(indices: List[String] = Nil): IndexStatusResult = {
    /*
          Retrieve the status of one or more indices
          (See :ref:`es-guide-reference-api-admin-indices-status`)

          :keyword indices: an index or a list of indices
    */
    val path = client.makeUrl(indices, path = "_status")
    client.doCall("GET", path).extract[IndexStatusResult]
  }

  def aliases(indices: List[String] = Nil): JValue = {
    /*
          Retrieve the aliases of one or more indices.
          ( See :ref:`es-guide-reference-api-admin-indices-aliases`)

          :keyword indices: an index or a list of indices

    */
    val path = client.makeUrl(indices, path = "_aliases")
    client.doCall("GET", path)
  }

  def create(index: String, settings: Settings = Settings(), mappings:Map[String, DocumentObjectField]=Map.empty[String, DocumentObjectField]): IndexCreateResult = {
    /*
        Creates an index with optional settings.
        :ref:`es-guide-reference-api-admin-indices-create-index`

        :param index: the name of the index
        :keyword settings: a settings object or a dict containing settings
    */

    var body:Map[String, Any] = Map("settings"-> settings)
    if (mappings.size>0) body += ("mappings"->mappings)

    client.doCall("PUT", index, body).extract[IndexCreateResult]
  }


  def createIndexIfMissing(index: String, settings: Settings = Settings(),
                           mappings:Map[String, DocumentObjectField]=Map.empty[String, DocumentObjectField]): IndexCreateResult = {
    /*
          Creates an index if it doesn't already exist.

          If supplied, settings must be a dictionary.

          :param index: the name of the index
          :keyword settings: a settings object or a dict containing settings
    */
//    try {
      create(index, settings, mappings)
//    } catch {
//      case e: IndexAlreadyExistsException => println("exception caught: " + e);
//        IndexCreateResult(false, true)
//    }
  }

  def delete(index: String): IndexDeleteResult = {
    /*
          Deletes an index.
          :ref:`es-guide-reference-api-admin-indices-delete-index`

          :param index: the name of the index

    */
    val json = client.doCall("DELETE", index)
    json.extract[IndexDeleteResult]
  }


  def existsIndex(index: String): Boolean = {
    /*
        Check if an index exists.
        (See :ref:`es-guide-reference-api-admin-indices-indices-exists`)

        :param index: the name of the index
    */
    try{
    client.doCall("HEAD", index)
    } catch {
      case nf:NotFound => return false
      case e:Throwable => throw e
    }
    true
  }

  def deleteIndexIfExists(index: String) = {
    /*
        Deletes an index if it exists.

        :param index: the name of the index

    */
    if (!existsIndex(index))
      delete(index)

  }

  def close(index: String): IndexCloseResult = {
    /*
        Close an index.
        (See :ref:`es-guide-reference-api-admin-indices-open-close`)


        :param index: the name of the index

    */
    client.doCall("POST", "/" + index + "/_close").extract[IndexCloseResult]

  }

  def open(index: String): IndexOpenResult = {
    /*
        Open an index.
        (See :ref:`es-guide-reference-api-admin-indices-open-close`)

        :param index: the name of the index
    */
    client.doCall("POST", "/" + index + "/_open").extract[IndexOpenResult]

  }

  def putMapping(indices: Seq[String], docType: String, mapping:JObject): JValue ={
    client.doCall("POST", client.makeUrl(indices=indices, docTypes=List(docType), path="_mapping"), body=mapping)
  }


  def deleteMapping(index: Seq[String], docType: Seq[String]): JValue ={
    /*
          Delete a typed JSON document type from a specific index.
          (See :ref:`es-guide-reference-api-admin-indices-delete-mapping`)

    */
    client.doCall("DELETE", client.makeUrl(index, docType))
  }

  def deleteMapping(index: String, docType: String): JValue = deleteMapping(List(index), List(docType))


  def updateSettings(index: String, newvalues: String): JValue = {
    /*
          Update Settings of an index.
          (See  :ref:`es-guide-reference-api-admin-indices-update-settings`)

    */
    client.doCall("PUT", client.makeUrl(List(index), path="_settings"), newvalues)
  }

  def optimize(indices: List[String] = Nil,
               wait_for_merge: Boolean = false,
               max_num_segments: Int = 0,
               only_expunge_deletes: Boolean = false,
               refresh: Boolean = false,
               flush: Boolean = false): IndexOptimizeResult = {

    /*
      Optimize one or more indices.
      (See :ref:`es-guide-reference-api-admin-indices-optimize`)


      :keyword indices: the list of indices to optimise.  If not supplied, all
                        default_indices are optimised.

      :keyword wait_for_merge: If True, the operation will not return until the merge has been completed.
                               Defaults to False.

      :keyword max_num_segments: The number of segments to optimize to. To fully optimize the index, set it to 1.
                                 Defaults to half the number configured by the merge policy (which in turn defaults
                                 to 10).


      :keyword only_expunge_deletes: Should the optimize process only expunge segments with deletes in it.
                                     In Lucene, a document is not deleted from a segment, just marked as deleted.
                                     During a merge process of segments, a new segment is created that does have
                                     those deletes.
                                     This flag allow to only merge segments that have deletes. Defaults to false.

      :keyword refresh: Should a refresh be performed after the optimize. Defaults to true.

      :keyword flush: Should a flush be performed after the optimize. Defaults to true.

*/
    val path = client.makeUrl(indices, path = "_optimize")
    val json = ("wait_for_merge" -> wait_for_merge) ~ ("only_expunge_deletes" -> only_expunge_deletes) ~ ("refresh" -> refresh) ~
      ("flush" -> flush) ~("max_num_segments", max_num_segments)


    client.doCall("POST", path, json).extract[IndexOptimizeResult]

  }


  def gatewaySnapshot(indices: List[String] = Nil): IndexGatewaySnapshotResult = {
    /*
          Gateway snapshot one or more indices
          (See :ref:`es-guide-reference-api-admin-indices-gateway-snapshot`)

          :keyword indices: a list of indices or None for default configured.
    */
    val path = client.makeUrl(indices, path = "_gateway/snapshot")
    client.doCall("POST", path).extract[IndexGatewaySnapshotResult]

  }


  def getSettings(index: Option[String] = None): JValue = {
    /*
        Returns the current settings for an index.
        (See :ref:`es-guide-reference-api-admin-indices-get-settings`)

    */
    val path = client.makeUrl(defaultIndex = index, path = "_settings")
    client.doCall("GET", path)

  }


}


