package es.mapping

import scala.collection.mutable
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 05/03/13
 * Time: 17:54
 */
class MultiField extends AbstractField {
  var fields: Map[String, Field] = Map()
  override val `type`: String = "multi_field"

  override def read(in: Map[String, Any]) {
    super.read(in)
    if (in.contains("fields")) {
      val props = mutable.HashMap[String, Field]()
      val records = in("fields").asInstanceOf[Map[String, Any]]
      for ((name, data) <- records) {
        props(name) = Mapper.processType(data.asInstanceOf[Map[String, Any]])
      }
      fields = props.toMap
    }
  }

  override def toJson: JObject = ("type" -> this.`type`) ~ ("fields" -> this.fields.map {
    case (key, value) => (key -> value.toJson)
  })
}
