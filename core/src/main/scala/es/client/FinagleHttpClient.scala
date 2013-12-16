package es.client

import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpRequest}
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.Http
import net.liftweb.json.JsonAST._
import net.liftweb.json.Printer._
import org.jboss.netty.util.CharsetUtil
import net.liftweb.json.JsonAST.JObject
import es.search.Search
import com.twitter.util.Await

object FinagleHttpClient {
  def apply(host: String): FinagleHttpClient = new FinagleHttpClient(List(new ServerAddress(host, 9200)))

  def apply(host: String, port: Int): FinagleHttpClient = new FinagleHttpClient(List(new ServerAddress(host, port)))

  def apply(host: String, port: Int, options: ESOptions): FinagleHttpClient = new FinagleHttpClient(List(new ServerAddress(host, port)), options)

  def apply(hosts: List[ServerAddress]): FinagleHttpClient = new FinagleHttpClient(hosts)
}

case class FinagleHttpClient(servers: List[ServerAddress] = List(new ServerAddress),
                             options: ESOptions = new ESOptions) extends Client {


  import com.twitter.conversions.time._

  lazy val hosts: String = servers.map {
    server => server.host + ":" + server.port
  } mkString (",")
  lazy val client: Service[HttpRequest, HttpResponse] = ClientBuilder()
    .codec(Http())
    .hosts(hosts) // If >1 host, client does simple load-balancing
    .hostConnectionLimit(5) // max number of connections at a time to a host
    .tcpConnectTimeout(2.second) // max time to spend establishing a TCP connection
    .retries(2) // (1) per-request retries
    //.reportTo(new OstrichStatsReceiver) // export host-level load data to ostrich
    //.logger(Logger.getLogger("http"))
    .build()

  def doCall(method: String, url: String, body: Any = None, queryArgs: Map[String, String] = Map.empty[String, String]): JValue = {
    import org.jboss.netty.buffer.ChannelBuffers
    import org.jboss.netty.handler.codec.http.{HttpMethod, HttpVersion, DefaultHttpRequest}
    import org.jboss.netty.util.CharsetUtil.UTF_8

    val validUrl = buildUrl(url, queryArgs)


    val req = method match {
      case "POST" => new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, validUrl)
      case "PUT" => new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, validUrl)
      case "DELETE" => new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, validUrl)
      case "GET" => new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, validUrl)
      case "HEAD" => new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.HEAD, validUrl)
      case _ => new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, validUrl)
    }
    req.headers().add("Content-Type", "application/json")
    bodyAsString(body)  match {
      case None =>
      case Some(s) =>
        //println("docall: data:" + s)
//        req.setHeader("Content-Length", s.length.toString) //.buildPost(wrappedBuffer(data))
        val b = ChannelBuffers.copiedBuffer(s, UTF_8)
        req.headers().set("Content-Length", s.length) //.buildPost(wrappedBuffer(data))
        req.setContent(b)
    }


    //val f: Try[HttpResponse] = client(req).get(20.seconds) // Client, send the request
    val future = client(req)

    // Handle the response:
    //    f onSuccess { res =>
    val resolved = Await.result(future, 1.second)

    import net.liftweb.json._
    val result = resolved.getContent.toString(CharsetUtil.UTF_8)
//    println(result)
    val res = parse(result)

    if (resolved.getStatus.getCode < 200 || resolved.getStatus.getCode >= 300) {

      throw buildException(res, resolved.getStatus.getCode)
    }

    res
  }

  def close: Unit = {
    client.close(10.seconds)
  }
}
