package es.refine


import es.orm._
import org.joda.time.DateTime

import es.mapping.{BaseObjectField, Field, Mapper}
import es.fixures.User
import es.orm.Annotations.Fk

@ESDocument
case class Refine(name: String, index: String, docType: String, sampleSize: Int = -1, @Fk createdBy: User,
                  creationDate: DateTime = DateTime.now(),
                  modificationDate: DateTime = DateTime.now(),position:Integer = 0,columns: List[Column]=Nil) extends ESObject[Refine] with CustomID{
  def meta = Refine

  def calcID(): Option[String] = Some(toUUID(name))


  lazy val mapping = Mapper.getDocumentType(index, docType).get



  def setColums= {
    val columns = mapping.properties.zipWithIndex.map{case(prop,idx)=> Column(idx, prop._1,prop._1,prop._2.`type`)}.toList
    this.copy(columns=columns)
  }



}

object Refine extends ESMeta[Refine]{}


@ESDocument
case class Column(position:Integer, fieldName:String, name:String, fieldType:String, modifiers:List[Modifier] = Nil ) extends ESObject[Column]
{
  def meta = Column
}
object Column extends ESMeta[Column]  {
}

@ESDocument
case class Modifier(name:String,createdBy: User, creationDate: DateTime = DateTime.now()) extends ESObject[Modifier]
{
  def meta = Modifier
}

object Modifier extends ESMeta[Modifier]  {
}


