package es.mapping

/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 05/03/13
 * Time: 17:54
 */

object LongField {
  def apply(index: Option[String]=Some("yes"), store: Option[Boolean]=Some(false))={
    val field=new LongField
    field.index=index
    field.store=store
    field
  }
}

class LongField extends AbstractField {
  override val `type`: String = "long"
}
