package es.client


import net.liftweb.json.JsonDSL._
import net.liftweb.json.Printer._
import scala.Some
import net.liftweb.json.DefaultFormats
import es.queries.MatchAllQuery
import es.client.responses._
import es.{DefaultESIdentifier, ESSerializers}
import es.actions.{BulkAction, UpdateAction, IndexOperationType, IndexAction}
import es.search.{QueryFilterCachable, Search}
import net.liftweb.json.Extraction._
import es.search.Search
import es.actions.UpdateAction
import scala.Some
import es.client.responses.CountResponse
import es.client.responses.DeleteByQueryResult
import es.client.responses.IndexResponse
import es.client.responses.DeleteResponse
import es.client.responses.GetResponse
import es.client.responses.HitResponse
import es.actions.IndexAction
import net.liftweb.json.ext.JodaTimeSerializers
import net.thenetplanet.common.uri.Uri
import es.mapping.DocumentObjectField


case class ServerAddress(host: String = "127.0.0.1", port: Int = 9200)

case class ESOptions(sniffClients: Boolean = true)


object Client {

  def apply(host: String, port: Int): Client = Client(host, port, true)

  def apply(host: String, port: Int, sniff: Boolean): Client = {
    port match {
      case p:Int if p >=9200 && p<9300 => new FinagleHttpClient(List(new ServerAddress(host, port)), ESOptions(sniff))
    }

  }

  def apply(hosts: List[String], port: Int): Client = {
    port match {
      case p:Int if p >=9200 && p<9300 => new FinagleHttpClient(hosts.map(new ServerAddress(_, port)).toList)
    }

  }


}


trait Client {

  import net.liftweb.json.JsonAST._

  implicit val formats = DefaultESIdentifier.formats
  lazy val admin = AdminManager(this)

  var bulkSize = 200

  protected var bulker: List[String] = List.empty[String]

  def doCall(method: String, url: String, body: Any = None, queryArgs: Map[String, String] = Map.empty[String, String]): JValue

  def close

  def bodyAsString(body: Any):Option[String]= body match {
      case None => None
      case null => None
      case s:String => Some(s)
      case search: Search => Some(compact(render(search.toJson)))
      case jobj: JObject => Some(compact(render(jobj)))
      case _ => Some(compact(render(decompose(body)(formats))))
  }

  def makeUrl(indices: String, docType: String, id: Option[String], path: String): String = {
    var url = s"/$indices/$docType/"
    id match {
      case None =>
      case Some(s) => url = url + "/" + s
    }
    path match {
      case "" =>
      case s: String => url = url + "/" + s
    }
    url
  }

  def makeUrl(indices: String, docType: String, id: String): String = s"/$indices/$docType/$id"

  def makeUrl(indices: String, docType: String, id: String, path: String): String = {
    var url = s"/$indices/$docType/$id"
    path match {
      case "" =>
      case s: String => url = url + "/" + s
    }
    url
  }

  def makeUrl(indices: Seq[String] = Nil, docTypes: Seq[String] = Nil, id: Option[String] = None, path: String = "", defaultIndex: Option[String] = None): String = {
    var url = ""
    indices match {
      case Nil =>
      case l: List[_] => url = url + "/" + l.mkString(",")
    }
    docTypes match {
      case Nil =>
      case l: List[_] => url = url + "/" + l.mkString(",")
    }
    id match {
      case None =>
      case Some(s) => url = url + "/" + s
    }
    path match {
      case "" =>
      case s: String => url = url + "/" + s
    }
    url
  }

  /* Get a typed JSON document from an index based on its id. */
  def get(index: String, docType: String, id: String, fields: List[String] = Nil, routing: Option[String] = None,
          params: Map[String, String] = Map()): GetResponse = {
    var vParams = params
    if (!fields.isEmpty)
      vParams += ("fields" -> fields.mkString(","))
    if (routing.isDefined)
      vParams += ("routing" -> routing.get)
    val res=doCall("GET", makeUrl(index, docType, id, ""), queryArgs = params)
    res.extract[GetResponse]
  }

  def index(index: String, docType: String, id: String, document: Map[String, Any]): IndexResponse = {
    import net.liftweb.json._
    val doc = decompose(document)(formats)
    this.index(index = index, docType = docType, id = Some(id), document = doc.asInstanceOf[JObject])
  }


  def index(index: String, docType: String, id: Option[String] = None, document: JObject, parent: Option[String] = None,
            routing: Option[String] = None,
            forceInsert: Boolean = false,
            opType: Option[IndexOperationType] = None,
            bulk: Boolean = false, version: Long = 0, querystringArgs: Map[String, String] = Map.empty[String, String]): IndexResponse = {
    var querystring = querystringArgs
    if (bulk) {
      var opcode = "index"
      if (forceInsert)
        opcode = "create"
      if (opType.isDefined)
        opcode = opType.get.name

      var data = ("_index" -> index) ~ ("_type" -> docType) ~ ("_routing" -> routing) ~ ("_parent" -> parent)

      id match {
        case Some(x) => data ~= ("_id" -> id)
        case _ =>
      }


      val header = (opcode -> data)


      addToBulker(compact(render(header)), compact(render(document)))


      return IndexResponse(true, "NA", "NA", "NA")
    }

    if (forceInsert)
      querystring = querystring + ("opType" -> "create")
    if (opType.isDefined)
      querystring = querystring + ("opType" -> opType.get.name)
    if (parent.isDefined)
      querystring = querystring + ("parent" -> parent.get)
    if (version > 0)
      querystring = querystring + ("version" -> version.toString)
    val method = id match {
      case Some(x) => "POST"
      case _ =>
        querystring -= "opType"
        "POST"
    }
    implicit val formats = DefaultFormats
    doCall(method, makeUrl(index, docType, id, ""), body = document, queryArgs = querystring).extract[IndexResponse]
  }

  def index(action: IndexAction): IndexResponse =
    index(index = action.index, docType = action.docType, id = action.id, document = action.document, parent = action.parent,
      routing = action.routing, forceInsert = action.forceInsert, bulk = false, version = action.version,
      opType = Some(action.opType), querystringArgs = action.querystringArgs
    )

  def update(action: UpdateAction): JObject = update(index = action.index, docType = action.docType, id = action.id, params = action.params)

  def update(index: String, docType: String, id: String, script: String = "", lang: String = "mvel",
             params: Map[String, Any] = Map.empty[String, Any], document: Option[JObject] = None, upsert: Option[JObject] = None): JObject = {
    val path = makeUrl(index, docType, id, path = "_update")
    var body =
      ("upsert" -> upsert) ~
        ("doc" -> document)

    if (!script.isEmpty)
      body = body merge ("script" -> script) ~ ("lang" -> lang)

    //if (params.isDefined) {
    //  body = body merge ("params" -> params.get.map {
    //    w => (w._1 -> w._2)
    //  })
    //}

    implicit val formats = DefaultFormats
    doCall("POST", path, body).extract[JObject]
  }

  /*
  Delete a typed JSON document from a specific index based on its id.
  If bulk is True, the delete operation is put in bulk mode.
   */
  def delete(index: String, docType: String, id: String, bulk: Boolean = false, routing: Option[String] = None,
             querystring_args: Option[Map[String, String]] = None): DeleteResponse = {
    var querystring = querystring_args getOrElse Map()
    if (routing.isDefined)
      querystring = querystring + ("routing" -> routing.get)
    if (bulk) {
      val header = ("delete" -> ("_index" -> index)
        ~ ("_type" -> docType)
        ~ ("_id" -> id)
        ~ ("_routing" -> routing))
      addToBulker(compact(render(header)))

      return DeleteResponse()
    }
    val path = makeUrl(index, docType, id)
    implicit val formats = DefaultFormats
    doCall("DELETE", path, queryArgs = querystring).extract[DeleteResponse]
  }

  /* Delete documents from one or more indices and one or more types based on a query. */
  def deleteByQuery(index: String, docType: String, query: QueryFilterCachable): DeleteByQueryResult =
    deleteByQuery(indices = List(index), docTypes = List(docType), query)

  def deleteByQuery(indices: Seq[String], docTypes: Seq[String], query: QueryFilterCachable): DeleteByQueryResult =
    deleteByQuery(indices = indices, docTypes = docTypes, query.toJson, params = Map.empty[String, String])

  def deleteByQuery(indices: Seq[String], docTypes: Seq[String], query: JObject, params: Map[String, String] = Map.empty[String, String]): DeleteByQueryResult = {
    val path = makeUrl(indices, docTypes, path = "_query")
    implicit val formats = DefaultFormats
    doCall("DELETE", path, query, queryArgs = params).extract[DeleteByQueryResult]
  }

  /* Execute a query against one or more indices and get hits count. */
  def count(indices: Seq[String], docTypes: Seq[String]): CountResponse =
    count(MatchAllQuery().toInnerJson, indices = indices, docTypes = docTypes)

  def count(query: QueryFilterCachable, indices: Seq[String], docTypes: Seq[String]): CountResponse =
    count(query.toJson, indices = indices, docTypes = docTypes)

  def count(query: JObject, indices: Seq[String] = Nil, docTypes: Seq[String] = Nil,
            params: Map[String, String] = Map.empty[String, String]): CountResponse = {
    implicit val formats = DefaultFormats
    doCall("GET", makeUrl(indices, docTypes, path = "_count"), query, queryArgs = params).extract[CountResponse]
  }

  /*Execute a search against one or more indices to get the resultset.

        `query` must be a Search object, a Query object, or a custom
        dictionary of search parameters using the query DSL to be passed
        directly.

  */
  def search(index: String, docType: String, size: Int): ESBaseCursor = {
    new NativeCursor(this, Search(query = MatchAllQuery(), size = size), List(index), List(docType))
  }

  def search(search: Search, indices: Seq[String] = Nil, docTypes: Seq[String] = Nil,
             params: Map[String, String] = Map.empty[String, String]): ESBaseCursor = {
    new NativeCursor(this, search, indices, docTypes, params)
  }


  /*
  Execute a search against one or more indices to get the search hits.
        `query` must be a Search object, a Query object, or a custom
        dictionary of search parameters using the query DSL to be passed
        directly.
   */
  def searchRaw(query: Search, indices: Seq[String] = Nil, docTypes: Seq[String] = Nil, routing: Option[String] = None,
                params: Map[String, String] = Map.empty[String, String]): JValue = {
    val url = makeUrl(indices, docTypes, path = "_search")
    doCall("POST", url, query, queryArgs = params)
  }

  def flushBulk() {
    if (bulker.size > 0) {
      val res = doCall("POST", "/_bulk", bulker.mkString("\n") + "\n")
      bulker = List()
    }
  }

  def addToBulker(action: BulkAction) {
    bulker = bulker ::: action.bulk
    if (bulker.size / 2 >= this.bulkSize)
      flushBulk()
  }

  def addToBulker(header: String, body: String = "") {
    if (body != "")
      bulker= bulker ::: List(header, body)
    else
      bulker = bulker ::: header :: Nil

    if (bulker.size / 2 >= this.bulkSize)
      flushBulk()
  }

  def scroll(scrollId: String, scrollTime: Option[String] = None): SearchResponse = {
    doCall("GET", "/_search/scroll", scrollId, queryArgs = Map("scroll" -> scrollTime.getOrElse("3m"))).extract[SearchResponse]
  }


  private def removeErrorType(errorType: String, str: String): String = str.substring(errorType.size + 1, str.size - 2)

  /*
  * Build an error
  */
  protected def buildException(data: JValue, status: Int = 0): Throwable = {
    implicit val formats = net.liftweb.json.DefaultFormats ++ JodaTimeSerializers.all
    //println(status)
    import es.exceptions._
    val exception = data match {
      case JNothing=>
        if (status==404) new NotFound() else new NotFound() //TODO improve
      case _ =>
        val error = data.extract[ErrorResponse]
        val errorType = error.error.split( """\[""")(0)
        errorType match {
          case "NoServerAvailable" => new NoServerAvailable(removeErrorType(errorType, error.error), error.status)
          case "InvalidQuery" => new InvalidQuery(removeErrorType(errorType, error.error), error.status)
          case "InvalidParameterQuery" => new InvalidParameterQuery(removeErrorType(errorType, error.error), error.status)
          case "QueryError" => new QueryError(removeErrorType(errorType, error.error), error.status)
          case "QueryParameterError" => new QueryParameterError(removeErrorType(errorType, error.error), error.status)
          case "ScriptFieldsError" => new ScriptFieldsError(removeErrorType(errorType, error.error), error.status)
          case "InvalidParameter" => new InvalidParameter(removeErrorType(errorType, error.error), error.status)
          case "MissingValueException" => new MissingValueException(removeErrorType(errorType, error.error), error.status)
          case "NotUniqueValueException" => new NotUniqueValueException(removeErrorType(errorType, error.error), error.status)
          case "ESIllegalArgumentException" => new ESIllegalArgumentException(removeErrorType(errorType, error.error), error.status)
          case "IndexMissingException" => new IndexMissingException(removeErrorType(errorType, error.error), error.status)
          case "NotFoundException" => new NotFoundException(removeErrorType(errorType, error.error), error.status)
          case "AlreadyExistsException" => new AlreadyExistsException(removeErrorType(errorType, error.error), error.status)
          case "IndexAlreadyExistsException" => new IndexAlreadyExistsException(removeErrorType(errorType, error.error), error.status)
          case "SearchPhaseExecutionException" => new SearchPhaseExecutionException(removeErrorType(errorType, error.error), error.status)
          case "ReplicationShardOperationFailedException" => new ReplicationShardOperationFailedException(removeErrorType(errorType, error.error), error.status)
          case "ClusterBlockException" => new ClusterBlockException(removeErrorType(errorType, error.error), error.status)
          case "MapperParsingException" => new MapperParsingException(removeErrorType(errorType, error.error), error.status)
          case "ReduceSearchPhaseException" => new ReduceSearchPhaseException(removeErrorType(errorType, error.error), error.status)
          case "VersionConflictEngineException" => new VersionConflictEngineException(removeErrorType(errorType, error.error), error.status)
          case "DocumentAlreadyExistsException" => new DocumentAlreadyExistsException(removeErrorType(errorType, error.error), error.status)
          case "DocumentAlreadyExistsEngineException" => new DocumentAlreadyExistsEngineException(removeErrorType(errorType, error.error), error.status)
          case "TypeMissingException" => new TypeMissingException(removeErrorType(errorType, error.error), error.status)
          case "MappedFieldNotFoundException" => new MappedFieldNotFoundException(removeErrorType(errorType, error.error), error.status)
          case _ => new ESException(removeErrorType(errorType, error.error), error.status)

        }

    }


    throw exception
  }

  protected def buildUrl(url: String, queryParameters: Map[String, Any]): String = {
    var result = url
    if (!url.startsWith("/")) result = "/" + url
    if (!queryParameters.isEmpty) {
      var uri = Uri("http://example.com" + result)
      uri = uri.addParams(queryParameters.toList)
      result = url + uri.toString()
    }

    result
  }

}

