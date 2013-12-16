package es

trait ESIdentifier {
  def jndiName: String

  override def toString() = "ESIdentifier(" + jndiName + ")"

  override def hashCode() = jndiName.hashCode()

  override def equals(other: Any): Boolean = other match {
    case mi: ESIdentifier => mi.jndiName == this.jndiName
    case _ => false
  }
}

case class ESIdentificator(jndiName:String) extends ESIdentifier{
  import net.liftweb.json.ext.JodaTimeSerializers

  implicit val formats = net.liftweb.json.DefaultFormats ++ JodaTimeSerializers.all ++ ESSerializers.all
}