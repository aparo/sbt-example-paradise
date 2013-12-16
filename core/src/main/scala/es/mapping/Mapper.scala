package es.mapping

import collection.mutable
import internal.Index
import es.Connections

//http://www.scalafied.com/105/default-and-customized-lift-json-type-hints
object Mapper {
  private var indices: Map[String, Index] = Map()

  def getDocumentType(index: String, mapping: String): Option[DocumentObjectField] = {
    if(indices.size==0) refresh()
    if (!indices.contains(index))
      return None
    if (indices(index).documentTypes.contains(mapping))
      return Some(indices(index).documentTypes(mapping))
    None
  }

  def populate(data: Map[String, Any], index: Option[String] = None) {
    if (index.isDefined) {
      val dataObjects = new mutable.HashMap[String, DocumentObjectField]()
      for ((x, y) <- data) {
        dataObjects(x) = processType(data = y.asInstanceOf[Map[String, Any]], isDocument = true).asInstanceOf[DocumentObjectField]
      }
      indices = indices + (index.get -> Index(index.get, dataObjects.toMap))
    } else {
      for ((x, y) <- data) {
        populate(y.asInstanceOf[Map[String, Any]], Some(x))
      }
    }
  }

  def processType(data: Map[String, Any], default: String = "object", isDocument: Boolean = false): Field = {
    if (isDocument) {
      val field = new DocumentObjectField()
      field.read(data)
      return field
    }

    val _type: String = data.getOrElse("type", default).asInstanceOf[String]

    val field: Field = _type match {
      case "string" => new StringField()
      case "boolean" => new BooleanField()
      case "short" => new ShortField()
      case "integer" => new IntegerField()
      case "long" => new LongField()
      case "float" => new FloatField()
      case "double" => new DoubleField()
      case "ip" => new IpField()
      case "nlp" => new NLPField()
      case "date" => new DateField()
      case "multi_field" => new MultiField()
      case "geo_point" => new GeoPointField()
      case "attachment" => new AttachmentField()
      case "link" => new LinkField()
      case "nested" => new NestedField
      case "object" => new ObjectField
      case "binary" => new BinaryField
    }
    field.read(data)
    field
  }

   def indexNames(skipSpecial:Boolean=true):Seq[String]={
     if(indices.size==0) refresh()
     var names = indices.keySet.toList
     if(skipSpecial)
       names = names.filterNot(_.startsWith("_"))
     names.sorted
   }

   def typeNames(index:String, skipEdges:Boolean=true):Seq[String]={
     if(indices.size==0) refresh()
     indices.get(index) match {
       case None => List()
       case Some(idx) =>
         var names = idx.documentTypes.keys.toList
         if (skipEdges) names = names.filterNot(_.endsWith("__edges"))
         names.sorted
     }
   }

   def refresh(){
     val state = Connections.getConnection().admin.cluster.state(filter_nodes = true, filter_routing_table = true, filter_blocks = true)
     indices=state.metadata.indices.map{case (name, index) => (name, Index(name, index.mappings))}.toMap
     //update mapping indexName and typeName
     indices.foreach{
       case (indexName, index) =>
          index.documentTypes.foreach{
            case (typeName, dof) =>
              dof.indexName=indexName
              dof.typeName=typeName
          }
     }


   }
}