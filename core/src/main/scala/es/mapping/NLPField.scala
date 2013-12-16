package es.mapping

/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 05/03/13
 * Time: 17:54
 */

class NLPField extends StringField {
  var language: Option[String] = None
  override val `type`: String = "nlp"

  override def read(in: Map[String, Any]) {
    super.read(in)

    if (in.contains("language"))
      language = Some(in("language").asInstanceOf[String])
  }
}
