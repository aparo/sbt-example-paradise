package es

import es.client.Client

object Connections {

  var defaultIndex:String="default"

  private var connections:Map[String, Client]=Map()

  def getConnection(name:String="default"):Client ={
    if(!connections.contains(name)){
      if(!connections.contains("default")){
        connections += ("default" -> ESDB.getDb(DefaultESIdentifier).get.client)
      }
      connections += (name -> connections("default"))
    }
    connections(name)
  }

  def addConnection(name:String, client:Client){
    connections += (name -> client)
  }

  def getIndexName(name:String)= name match {
    case "default" => defaultIndex
    case _ => name
  }
}
