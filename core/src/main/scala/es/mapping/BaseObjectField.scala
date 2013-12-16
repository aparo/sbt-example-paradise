package es.mapping

import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import scala.collection.mutable

abstract class BaseObjectField extends Field {
  var properties: Map[String, Field] = Map()
  var dynamic: Option[Boolean] = None
  var enabled: Option[Boolean] = None
  var include_in_all: Option[Boolean] = None
  var dynamic_templates: Option[Map[String, Any]] = None
  var include_in_parent: Option[Boolean] = None
  var include_in_root: Option[Boolean] = None
  var index_name: Option[String] = None

    override def read(in: Map[String, Any]) {
      super.read(in)
      if (in.contains("dynamic"))
       dynamic= Some(in("dynamic") match {
          case b:Boolean => b
          case "false" => false
          case _ => true
        } )

      if (in.contains("enabled"))
        enabled = Some(in("enabled").asInstanceOf[Boolean])
      if (in.contains("include_in_all"))
        include_in_all = Some(in("include_in_all").asInstanceOf[Boolean])
      if (in.contains("include_in_parent"))
        include_in_parent = Some(in("include_in_parent").asInstanceOf[Boolean])
      if (in.contains("include_in_root"))
        include_in_root = Some(in("include_in_root").asInstanceOf[Boolean])
      if (in.contains("index_name"))
        index_name = Some(in("index_name").asInstanceOf[String])
      if (in.contains("properties")) {
        val props = mutable.HashMap[String, Field]()
        val records = in("properties").asInstanceOf[Map[String, Any]]
        properties = records.map{case (name, data)=> (name, Mapper.processType(data.asInstanceOf[Map[String, Any]]))}.toMap
      }
      //TODO dynamic_templates
    }

  override def toJson: JObject = ("dynamic" -> dynamic) ~
    ("enabled" -> enabled) ~
    ("include_in_all" -> include_in_all) ~
    ("include_in_parent" -> include_in_parent) ~
    ("include_in_root" -> include_in_root) ~
    ("index_name" -> index_name) ~
      ("properties" -> properties.map {
        case (key, value) => (key -> value.toJson)
      })

    //def toBodyJson: JObject = (name -> this.toJson)

    def add(name:String, property:Field){
      properties += (name -> property)
    }
}
