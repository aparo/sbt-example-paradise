package es.mapping

/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 05/03/13
 * Time: 17:53
 */

object IntegerField {
  def apply(index: Option[String]=Some("yes"), store: Option[Boolean]=Some(false))={
    val field=new IntegerField
    field.index=index
    field.store=store
    field
  }
}

class IntegerField extends AbstractField {
  override val `type`: String = "integer"
}
