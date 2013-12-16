package es.orm

import org.joda.time.DateTime
import es.utils.Mapper


trait IDFetchable {
  def getById(index: String, typeName: String, id: String): Option[Any]
  def getFromMap(data:Map[String, Any]): Option[Any]

  def typeName: String
}

case class FldOptions(var metadata: Map[String, Any] = Map.empty[String, Any], var index: String = "analyzed",
                      var foreignKey: Boolean = false, var store: Boolean = false,var nested: Boolean = false,
                      var embedded: Boolean = false, var multiple: Boolean = false,
                      var required: Boolean = true, var default: Option[Any] = None)


case class FieldDescription[T, A](name: String, clazz: Class[_], metadata: Map[String, Any] = Map.empty[String, Any],
                                  default: Option[Any] = None, foreignKey: Boolean = false,
                                  index: String = "yes", store: Boolean = false,
                                  nested: Boolean = false, embedded: Boolean = false,
                                  required: Boolean = false, multiple: Boolean = false,
                                  builder: IDFetchable)  {
  def toMap=Mapper.toMap(this)

  def toType(obj: Any, concreteIndex: String): Any =
    obj match {
      case Some(value) =>
        value match {
          case None if (!this.required) => None
          case Nil if (!this.required) => Nil
          case _ if (this.foreignKey) =>
            this.multiple match {
              case true =>
                val v = (for (value <- value.asInstanceOf[List[String]].map(s => this.getById(concreteIndex, s))) yield value.get).toList
                if (v.length == 0) Nil else v.toList
              case _ =>
                val v = this.getById(concreteIndex, value.toString)
                if (!this.required) {
                  v match {
                    case None => None
                    case _ => v
                  }
                } else {
                  v match {
                    case None => v
                    case _ => v.get
                  }
                }
            }
          case _ if (this.embedded || this.nested) =>
            this.multiple match {
              case true =>
                val v = (for (value <- value.asInstanceOf[List[Map[String,Any]]].map(s => this.getFromMap(s))) yield value.get).toList
                if (v.length == 0) Nil else v.toList
              case _ =>
                val v = this.getFromMap(value.asInstanceOf[Map[String, Any]])
                if (!this.required) {
                  v match {
                    case None => None
                    case _ => v
                  }
                } else {
                  v match {
                    case None => v
                    case _ => v.get
                  }
                }
            }
          case _ if (this.clazz == classOf[DateTime]) =>

            this.multiple match {
              case true =>
                val v = value.asInstanceOf[List[String]].map(s => DateTime.parse(s.toString)).toList
                if (v.length == 0) Nil else v.toList
              case _ =>
                val v = DateTime.parse(value.toString)
                if (!this.required) {
                  Some(v)

                } else {
                  v
                }
            }
          case _ =>
            if (this.required)
              value.asInstanceOf[T]
            else
              Some(value.asInstanceOf[T])
        }
      case _ =>
        if (!this.required) {
          if (this.multiple) Nil else None
        } else {
          None //raise exception1!!! a field is missing
        }

    }

  //    obj.asInstanceOf[T]
  //  }

  def getById(index: String, id: String): Option[A] = builder.getById(index, builder.typeName, id) match {
    case None =>
      None
    case Some(v) => Some(v.asInstanceOf[A])
  }

  def getFromMap(data:Map[String, Any]): Option[A] = builder.getFromMap(data) match {
    case None =>
      None
    case Some(v) => Some(v.asInstanceOf[A])
  }

}

