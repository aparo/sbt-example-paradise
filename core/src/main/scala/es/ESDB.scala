package es

import java.util.concurrent.ConcurrentHashMap
import es.exceptions.ESException

/*
* Main ES object
*/
object ESDB {


  /*
  * HashMap of ESAddresses, keyed by ESIdentifier
  */
  private val dbs = new ConcurrentHashMap[ESIdentifier, ES]

  /*
  * Define a ES db
  */
  def defineDb(name: ESIdentifier, address: ES) {
    dbs.put(name, address)
  }

  /*
   * Define a ES db using a standard ES instance.
  def defineDb(name: ESIdentifier, cli: Service[HttpRequest, HttpResponse], dbName: String) {
    dbs.put(name, ES(new BaseClient {
      def client = cli
    }, dbName))
  }
   */

  /*
  * Get a ES reference
  */
  def getDb(name: ESIdentifier): Option[ES] = dbs.get(name) match {
    case null => None
    case ma: ES => Some(ma)
  }

  /*
  * Get a ES collection. Gets a ES db first.
  private def getCollection(name: ESIdentifier, typeName: String): Option[ESCollection] = getDb(name) match {
    case Some(esIndexAddress) => Some(ESCollection(esIndexAddress.host, esIndexAddress.index, typeName))
    case _ => None
  }
  */

  /**
   * Executes function {@code f} with the es db named {@code name}.
   */
  def use[T](name: ESIdentifier)(f: (ES) => T): T = {


    val db = getDb(name) match {
      case Some(es) => es
      case _ =>
        throw new ESException("ES not found: " + name.toString)
    }

    f(db)
  }

  /**
   * Executes function {@code f} with the es named {@code name}. Uses the default echidnasearchIdentifier
   */
  def use[T](f: (ES) => T): T = {


    val db = getDb(DefaultESIdentifier) match {
      case Some(es) => es
      case _ => throw new ESException("ES not found: " + DefaultESIdentifier.toString)
    }

    f(db)
  }

  /**
   * Executes function {@code f} with the es named {@code name} and collection names {@code typeName}.
   * Gets a collection for you.
  def useCollection[T](name: ESIdentifier, typeName: String)(f: (ESCollection) => T): T = {

    val coll = getCollection(name, typeName) match {
      case Some(collection) => collection
      case _ => throw new ESException("Collection not found: " + typeName + ". ESIdentifier: " + name.toString)
    }

    f(coll)
  }
   */

  /**
   * Same as above except uses DefaultESIdentifier
  def useCollection[T](typeName: String)(f: (ESCollection) => T): T = {

    val coll = getCollection(DefaultESIdentifier, typeName) match {
      case Some(collection) => collection
      case _ => throw new ESException("Collection not found: " + typeName + ". ESIdentifier: " + DefaultESIdentifier.toString)
    }

    f(coll)
  }
   */

  /**
   * Executes function {@code f} with the es db named {@code name}. Uses the same socket
   * for the entire function block. Allows multiple operations on the same thread/socket connection
   * and the use of getLastError.
   */
  def useSession[T](name: ESIdentifier)(f: (ES) => T): T = {

    val db = getDb(name) match {
      case Some(es) => es
      case _ => throw new ESException("ES not found: " + name.toString)
    }

    // start the request
    //db.requestStart
    try {
      f(db)
    }
    finally {
      // end the request
      //db.requestDone
    }
  }

  /**
   * Same as above except uses DefaultESIdentifier
   */
  def useSession[T](f: (ES) => T): T = {


    val db = getDb(DefaultESIdentifier) match {
      case Some(es) => es
      case _ => throw new ESException("ES not found: " + DefaultESIdentifier.toString)
    }

    // start the request
    //db.requestStart
    try {
      f(db)
    }
    finally {
      // end the request
      //db.requestDone
    }
  }

  //
  def close {
    dbs.clear
  }
}
