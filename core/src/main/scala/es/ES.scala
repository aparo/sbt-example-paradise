package es

import es.client.Client
import es.actions.BulkAction

/*
* Wrapper for getting a reference to a db from the given ES instance
*/
case class ES(client: Client, index: String) {
  var dirty=false

  createDatabase

  def dropDatabase {
    client.admin.indices.delete(index)
    dirty=false
  }


  def createDatabase {
    if(!client.admin.indices.existsIndex(index)){
      client.admin.indices.create(index)
      dirty=false
    }
  }

  def addToBulk(action:BulkAction){
    client.addToBulker(action)
    dirty=true
  }

  def refresh(){
    client.flushBulk()
    client.admin.indices.refresh(List(index))
    dirty=false
  }

}