package es

object DefaultESIdentifier extends ESIdentifier {

  val jndiName = "default"
  import net.liftweb.json.ext.JodaTimeSerializers

  implicit val formats = net.liftweb.json.DefaultFormats ++ JodaTimeSerializers.all ++ ESSerializers.all

}

