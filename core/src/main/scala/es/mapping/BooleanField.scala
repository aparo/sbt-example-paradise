package es.mapping

import scala.Option

/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 05/03/13
 * Time: 17:52
 */

object BooleanField {
  def apply(index: Option[String]=Some("yes"), store: Option[Boolean]=Some(false))={
    val field=new BooleanField
    field.index=index
    field.store=store
    field
  }
}

class BooleanField extends AbstractField {
  var null_value: Option[Any] = None
  var include_in_all: Option[Boolean] = None

  override val `type`: String = "boolean"

  override def read(in: Map[String, Any]) {
    super.read(in)
    if (in.contains("null_value"))
      null_value = Some(in("null_value"))
    if (in.contains("include_in_all"))
      include_in_all = Some(in("include_in_all").asInstanceOf[Boolean])
  }
}
