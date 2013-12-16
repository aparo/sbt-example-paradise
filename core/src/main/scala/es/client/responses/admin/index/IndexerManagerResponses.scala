package es.client.responses.admin.index


import es.client.responses.{Indices, Shards}

case class IndexCreateResult (ok:Option[Boolean], acknowledged:Option[Boolean], error:Option[String], status:Option[BigInt]){
  def getOk:Boolean = ok.getOrElse(false)
  def getClearError:String = error.getOrElse("No Error").replace("IndexAlreadyExistsException[","").replaceAll("]$","")
}
case class IndexDeleteResult (ok:Boolean, acknowledged:Boolean)
case class IndexOpenResult (ok:Boolean, acknowledged:Boolean)
case class IndexCloseResult (ok:Boolean, acknowledged:Boolean)

case class IndexAliasResult (ok:Boolean, acknowledged:Boolean)
case class IndexMappingResult (ok:Boolean, acknowledged:Boolean)
case class IndexOptimizeResult (ok:Boolean, _shards:Shards)
case class IndexGatewaySnapshotResult (ok:Boolean, acknowledged:Boolean)


case class IndexRefreshResult(ok:Boolean, _shards:Shards)
case class IndexFlushResult(ok:Boolean, _shards:Shards)
case class IndexStatsResult(ok:Boolean, _shards:Shards)
case class IndexStatusResult(ok:Boolean, _shards:Shards, indices:Map[String,Indices]) {
  def getTotalNumDocs:Long = indices.map(_._2.docs.num_docs).sum
  def getTotalSize:Long = indices.map(_._2.index.size_in_bytes).sum
  def getIndex(index:String) = indices.get(index)
}
