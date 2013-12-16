package es.mapping.internal

import es.mapping.Field

class TypeField(name: Option[String] = None,index: Option[Boolean] = None, store: Option[Boolean] = None, path: Option[String]= None) extends Field {
  override val `type`: String = "_type"

}
