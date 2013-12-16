package es.orm

import com.thoughtworks.paranamer.{BytecodeReadingParanamer, CachingParanamer}
import net.liftweb.json.Serialization._
import es._
import es.mapping._
import java.lang.reflect.{Type, ParameterizedType}
import scala.Some
import es.mapping.internal.AllField
import scala.tools.scalap.scalax.rules.scalasig.MethodSymbol
import scala.reflect.runtime.universe._
import net.liftweb.json._
import scala.reflect.ClassTag
import es.client.responses.{GetResponse, HitResponse}
import es.actions.{Index, Create, IndexAction}
import es.search.Filter
import es.orm.utils.UUID
import net.liftweb.common.Box

trait ESObject[BaseDocument <: WithId] extends WithId {
  self: BaseDocument =>

  def meta: ESMeta[BaseDocument]

  // convert class to a json value
  def asJObject()(implicit formats: Formats): JObject = meta.toJObject(this)

  def save(bulk: Boolean = false, forceCreate: Boolean = false):BaseDocument= {
    meta.save(self, bulk = bulk, forceCreate = forceCreate)
  }

  def delete(bulk: Boolean = false) {
    meta.delete(self, bulk = bulk)
  }

  def toMap = meta.toMap(this)

  def toJson = meta.toJson(this)

  def fullID():String=s"${_index.get}:${_type.get}:${_id.get}"
}

trait MappingMixin {
  def getMapping: DocumentObjectField

  def fields: List[FieldDescription[_, _]]=List.empty[FieldDescription[_, _]]
}

abstract class ESMeta[BaseDocument <: WithId](implicit mf: Manifest[BaseDocument]) extends MappingMixin with IDFetchable {

  import net.liftweb.json.Extraction._

  private def echidnasearchIdentifier: ESIdentifier = DefaultESIdentifier

  def esIndex: String = "default"

  private lazy val innerTypeName:String = {
    var tokens = mf.runtimeClass.getName.toLowerCase().split("\\.").toList
    if(tokens.contains("model")){
      tokens = tokens(tokens.indexOf("model")-1) :: tokens.last :: Nil
    }
    tokens.mkString("_")
  }

  def typeName: String = innerTypeName

  val pn = new CachingParanamer(new BytecodeReadingParanamer)

  def fromJsonOk(json: String): BaseDocument = {
    implicit val formats = es.orm.ModelSerializers.formats
    import net.liftweb.json.Serialization.read
    read[BaseDocument](json)
  }

  def fromJson(json: String, index: Option[String]): BaseDocument = {
    //implicit val formats = es.orm.ModelSerializers.formats
    implicit val formats = DefaultESIdentifier.formats
    import net.liftweb.json._
    val obj = JsonParser.parse(json).asInstanceOf[JObject].values
    fromMap(obj, index)
  }

  def fromJson(json: JObject, index: Option[String]): BaseDocument = {
    fromMap(json.values, index)
  }

  def fill[T](m: Map[String, AnyRef])(implicit mf: ClassTag[T]): Option[T] = {
    val ctor = mf.runtimeClass.getDeclaredConstructors.headOption
    val parameters = pn.lookupParameterNames(ctor.get)
    val args = parameters.map(m)
    Some(ctor.get.newInstance(args: _*).asInstanceOf[T])
  }

  def getConcreteIndex(index: Option[String]): String = {
    useDb {
      db =>
        index match {
          case Some(i) => i
          case _ => db.index
        }
    }
  }

  lazy val fieldNames = fields.map(_.name)

  def getFromMap(m: Map[String, Any]): Option[Any] = Some(fromMap(m, None))

  def fromMap(m: Map[String, Any], index: Option[String]): BaseDocument = {
    var extra: Map[String, AnyRef] = Map.empty[String, AnyRef]
    val concreteIndex = getConcreteIndex(index)
    val ctor = mf.runtimeClass.getDeclaredConstructors.headOption
    val parameters = pn.lookupParameterNames(ctor.get)
    val args = parameters.map {
      name =>
        val field = getFieldByName(name).get
        field.toType(m.get(name), concreteIndex)
    }
      .map {
      v =>
        v match {
          case x: BigInt => x.toInt.asInstanceOf[java.lang.Integer]
          case _ => v.asInstanceOf[Object]
        }
    }
    //        if (!internalNames.contains(name)) {
    //    extra += (name -> m.get(name).get)
    //  } else {

    ctor.get.newInstance(args: _*).asInstanceOf[BaseDocument]

  }

  private var _additionalJFields: List[JField] = Nil

  // convert class to a JObject
  def toJObject(in: BaseDocument)(implicit formats: Formats): JObject =
    decompose(in)(formats).asInstanceOf[JObject]

  /** Extra fields to add to the encoded object, such as type. Default is none (Nil) */
  def fixedAdditionalJFields: List[JField] = Nil

  /**
   * Additional fields that are not represented by Record fields, nor are fixed additional fields.
   * Default implementation is for preserving unknown fields across read/write
   */
  def additionalJFields: List[JField] = _additionalJFields

  /**
   * Handle any additional fields that are not represented by Record fields when decoding from a JObject.
   * Default implementation preserves the fields intact and returns them via additionalJFields
   */
  def additionalJFields_=(fields: List[JField]): Unit = _additionalJFields = fields

  /*
 * Use the db associated with this Meta.
 */
  def useDb[T](f: ES => T) = ESDB.use(echidnasearchIdentifier)(f)


  private def builder(hit: HitResponse): BaseDocument = {
    val obj = this.fromJson(hit.source.asInstanceOf[JObject], Some(hit.index))
    obj._id = Some(hit.id)
    obj._type = Some(hit.docType)
    obj._index = Some(hit.index)
    obj._version = hit.version.getOrElse(-1)
    obj
  }

  def toJson(obj: BaseDocument): String = {
    implicit val formats = DefaultESIdentifier.formats
    write(this.toMap(obj))
  }

  def save(obj: BaseDocument, bulk: Boolean = false, forceCreate: Boolean = false):BaseDocument= {
    useDb {
      db =>
        implicit val formats = DefaultESIdentifier.formats
        val source: JValue = decompose(this.toMap(obj))
        val indexAction=IndexAction(index=obj._index.getOrElse(db.index), docType=obj._type.getOrElse(this.typeName),
          document=source.asInstanceOf[JObject],
          id=obj._id match {
          case None => obj match {
            case c: CustomID =>
                val id = c.calcID()
                obj._id = id
                id
              case _ => None
          }
          case Some(id) =>
              obj._id
            case _ => None
          },
          version = obj._version,
          opType = if (forceCreate) Create else Index
        )


        bulk match {
          case true =>
            db.addToBulk(indexAction)
          case false =>
            val resp = db.client.index(indexAction)
            obj._id=Some(resp._id)
            obj._type=Some(resp._type)
            obj._index=Some(resp._index)
            obj._version=resp._version

        }
      obj
    }
  }


  private def processGetResponse(response: GetResponse): Option[BaseDocument] = {
    if (!response.exists) return None
    val obj = this.fromJson(response.source.asInstanceOf[JObject], Some(response.index))
    obj._id = Some(response.id)
    obj._type = Some(response.docType)
    obj._index = Some(response.index)
    obj._version = response.version
    Some(obj)
  }

  def getByIdHash(id: String): Option[BaseDocument] = useDb {
    db =>
      val response = db.client.get(db.index, this.typeName, UUID.nameUUIDFromString(id))
      processGetResponse(response)
  }

  def getById(id: String): Option[BaseDocument] = useDb {
    db =>
      val response = db.client.get(db.index, this.typeName, id)
      processGetResponse(response)
  }

  def getById(index: String, typeName: String, id: String): Option[BaseDocument] = useDb {
    db =>
      val response = db.client.get(index, typeName, id)
      processGetResponse(response)
  }

  /* drop this document collection */
  def drop: Unit = useDb {
    db =>
      db.client.admin.indices.deleteMapping(List(db.index), List(this.typeName))
  }

  /* drop this document collection */
  def refresh: Unit = useDb {
    db =>
      db.refresh()
  }


  def putMapping: Unit = useDb {
    db =>
      implicit val formats = DefaultESIdentifier.formats
      import net.liftweb.json._
      db.client.admin.indices.putMapping(List(db.index), this.typeName, JObject(List(JField(this.typeName, getMapping.toJson))))
  }

  def getMapping: DocumentObjectField = {
    import es.mapping.MappingBuilder
    val doc = new DocumentObjectField()
    val allField = new AllField()
    allField.enabled = false
    doc._all = Some(allField)
    for {
      ctor <- mf.runtimeClass.getDeclaredConstructors.headOption //.filter(m => m.getParameterTypes.forall(classOf[String] ==)).headOption
      parameters = pn.lookupParameterNames(ctor)

    } {
//      val types = ctor.getGenericParameterTypes.map {
//        gt =>
//          gt match {
//            case x: ParameterizedType if x.getRawType.toString != "interface scala.collection.immutable.Map" =>
//              x.getActualTypeArguments()(0) match {
//                case x: Class[_] => x.asInstanceOf[Class[_]]
//              }
//            case _ =>
//              gt
//          }
//      }.toList
//      MappingBuilder.createMapping(doc, parameters, this.fields.map(f => f.clazz), this.fields)
            MappingBuilder.createMapping(doc, this.fields.map(_.name), this.fields.map(f => f.clazz), this.fields)
    }
    doc
  }

  def getMethods[T: TypeTag] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m
  }.toList

  def toMap(record: BaseDocument): Map[String, Any] = MappingBuilder.dumpAsMap(record, fields)

  def getFieldByName(name: String): Option[FieldDescription[_, _]] = {
    fields.filter(_.name == name).map(v => return Some(v))
    None
  }

  def delete(obj: BaseDocument, bulk: Boolean = false) {
    useDb {
      db =>
        db.client.delete(obj._index.getOrElse(db.index), obj._type.getOrElse(this.typeName), obj._id.get)
    }
  }

  def delete_!(inst: BaseDocument, refresh: Boolean = false): Boolean = {
    //    foreachCallback(inst, _.beforeDelete)
    delete(inst)
    //    foreachCallback(inst, _.afterDelete)
    true
  }


  def find(id:String):Box[BaseDocument]=getById(id)


}
