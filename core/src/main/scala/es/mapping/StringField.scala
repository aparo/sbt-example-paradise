package es.mapping

object StringField {
  def apply(index: Option[String] = Some("not_analyzed"), store: Option[Boolean] = Some(false)): StringField = {
    val field = new StringField()
    field.index = index
    field.store = store
    field
  }

  def noIndex(name: String): StringField = {
    val field = new StringField()
    field.index = Some("no")
    field.store = Some(false)
    field
  }

  def multi(name: String, store: Boolean): MultiField = {
    val field = new StringField()
    field.index = Some("not_analyzed")
    field.store = Some(store)
    val fieldTk = new StringField()
    fieldTk.index = Some("analyzed")
    fieldTk.store = Some(false)
    val multi = new MultiField()
    multi.fields = Map(
      name -> field, name + ".tk" -> fieldTk)
    multi
  }

}

class StringField extends AbstractField {
  var null_value: Option[String] = None
  var include_in_all: Option[Boolean] = None
  var store_term_vector: Option[Boolean] = None
  var store_term_vector_positions: Option[Boolean] = None
  var store_term_vector_offsets: Option[Boolean] = None
  var omit_norms: Option[Boolean] = None
  var omit_term_freq_and_positions: Option[Boolean] = None
  override val `type`: String = "string"

  override def read(in: Map[String, Any]) {
    super.read(in)

    if (in.contains("null_value"))
      null_value = Some(in("null_value").asInstanceOf[String])
    if (in.contains("include_in_all"))
      include_in_all = Some(in("include_in_all").asInstanceOf[Boolean])
    if (in.contains("store_term_vector"))
      store_term_vector = Some(in("store_term_vector").asInstanceOf[Boolean])
    if (in.contains("store_term_vector_positions"))
      store_term_vector_positions = Some(in("store_term_vector_positions").asInstanceOf[Boolean])
    if (in.contains("store_term_vector_offsets"))
      store_term_vector_offsets = Some(in("store_term_vector_offsets").asInstanceOf[Boolean])
    if (in.contains("omit_norms"))
      omit_norms = Some(in("omit_norms").asInstanceOf[Boolean])
    if (in.contains("omit_term_freq_and_positions"))
      omit_term_freq_and_positions = Some(in("omit_term_freq_and_positions").asInstanceOf[Boolean])
  }
}
