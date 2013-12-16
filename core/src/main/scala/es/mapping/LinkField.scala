package es.mapping

import scala.Predef._
import scala.Some

/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 05/03/13
 * Time: 17:54
 */
class LinkField extends AbstractField {
  var null_value: Option[Any] = None
  var dest_type: Option[String] = None
  var dest_index: Option[String] = None
  override val `type`: String = "link"


  override def read(in: Map[String, Any]) {
    super.read(in)
    if (in.contains("null_value"))
      null_value = Some(in("null_value"))
    if (in.contains("dest_type"))
      dest_type = Some(in("dest_type").asInstanceOf[String])
    if (in.contains("dest_index"))
      dest_index = Some(in("dest_index").asInstanceOf[String])
  }
}
