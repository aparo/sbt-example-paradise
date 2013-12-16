package es.mapping

abstract class AbstractNumericField extends AbstractField {
  var null_value: Option[Any] = None
  var include_in_all: Option[Boolean] = None
  var precision_step: Int = 4
  var numeric_resolution: Option[Int] = None
  var ignore_malformed: Option[Boolean] = None

  override val `type`: String = "numeric"

  override def read(in: Map[String, Any]) {
    super.read(in)
    if (in.contains("null_value"))
      null_value = Some(in("null_value"))
    if (in.contains("include_in_all"))
      include_in_all = Some(in("include_in_all").asInstanceOf[Boolean])
    if (in.contains("precision_step"))
      precision_step = in("precision_step").asInstanceOf[Int]
    if (in.contains("numeric_resolution"))
      numeric_resolution = Some(in("numeric_resolution").asInstanceOf[Int])
    if (in.contains("ignore_malformed"))
      ignore_malformed = Some(in("ignore_malformed").asInstanceOf[Boolean])
  }
}
