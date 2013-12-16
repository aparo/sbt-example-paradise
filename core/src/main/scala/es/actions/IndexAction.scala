package es.actions

import es.DefaultESIdentifier
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Printer._
import net.liftweb.json.Extraction._

abstract class IndexOperationType(val name: String)

case object Create extends IndexOperationType("create")
case object Index extends IndexOperationType("index")


sealed trait BulkAction {
  def bulk:List[String]
}
case class IndexAction(index:String, docType:String, id:Option[String]=None, document:JObject,
                       version:Long = -1, parent: Option[String] = None,
                       routing: Option[String] = None,
                       forceInsert: Boolean = false,
                       opType:IndexOperationType = Index, querystringArgs: Map[String, String] = Map.empty[String,String]
                        ) extends BulkAction {
  def bulk:List[String]={
    implicit val formats = DefaultESIdentifier.formats
    var header:Map[String, Any] = Map("_index" -> index,"_type" -> docType,"_id" -> id)

    if(parent.isDefined){
      header += ("parent" -> parent)
    }
    if(routing.isDefined){
      header += ("routing" -> parent)
    }
    if(version != -1){
      header += ("version" -> version)
    }

    if(!querystringArgs.isEmpty){
      header ++= querystringArgs
    }
    val operation=if(forceInsert) Index.name else opType.name

    List(compact(render(decompose(Map(operation -> header)))), compact(render(document)))
  }
}

case class UpdateAction(index:String, docType:String, id:String,
                        script:String="", language:String="mvel",
                        params: Map[String, Any]=Map.empty[String, Any],
                        doc: Map[String, Any]=Map.empty[String, Any]
                         ) extends BulkAction {

  def bulk:List[String]={
    //{ "update" : {"_id" : "1", "_type" : "type1", "_index" : "index1"} }
    //{ "doc" : {"field2" : "value2"} }
    implicit val formats = DefaultESIdentifier.formats
    val header = Map("update" -> Map("_index" -> index,"_type" -> docType,"_id" -> id))

    var body=Map.empty[String, Any]

    if(!script.isEmpty){
      body ++= Map("script" -> script, "language" -> language, "params" -> params)
    }
    if(!doc.isEmpty){
      body += ("doc" -> doc)
    }
    List(compact(render(header)), compact(render(decompose(body))))
  }

}

case class DeleteAction(index:String, docType:String, id:String) extends BulkAction {
  def bulk:List[String]={
    //{ "delete" : { "_index" : "test", "_type" : "type1", "_id" : "2" } }
    implicit val formats = DefaultESIdentifier.formats
    val json = Map("delete" -> Map("_index" -> index,"_type" -> docType,"_id" -> id))
    List(compact(render(json)))
  }

}
