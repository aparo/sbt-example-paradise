package es.mapping

object DateField {
  def apply(index: Option[String] = Some("yes"), store: Option[Boolean] = Some(false)): DateField = {
    val field = new DateField()
    field.index = index
    field.store = store
    field
  }

  def noIndex: DateField = {
    val field = new DateField()
    field.index = Some("no")
    field.store = Some(false)
    field
  }

}

class DateField extends AbstractField {
  var format: Option[String] = None
  override val `type`: String = "date"

  override def read(in: Map[String, Any]) {
    super.read(in)
    if (in.contains("format"))
      format = Some(in("format").asInstanceOf[String])
  }
}