package es.orm

import es.orm.utils.UUID

trait WithId {
  var _id: Option[String]=None
  var _index: Option[String]=None
  var _type: Option[String]=None
  var _version: Long = -1
}

trait CustomID {
  def toUUID(data:String)=UUID.nameUUIDFromString(data)
  def calcID(): Option[String]
}

