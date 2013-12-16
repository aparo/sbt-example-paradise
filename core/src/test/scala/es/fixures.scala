package es.fixures

import es.orm._
import es.fields._
import org.joda.time.DateTime
import es.orm.utils.UUID
import es.fields.Email
import es.orm.Annotations.{NoIndex, Fk}

@ESDocument
case class Metadata(name:String) extends ESObject[Metadata]{
  def meta=Metadata

}

object Metadata extends ESMeta[Metadata] with ESReflect[Metadata]{
  
}

@ESDocument
case class Person(
                   age: Int, @Fk father: Option[Person] = None,
                   @Fk children: List[Person] = Nil,
                   date:Option[DateTime] = None,
                   name:Option[String] = None,
                   metadata:Map[String,Metadata] = Map.empty,
                   value:Option[Int]=None, email:Email="test@test.com"
                   )
  extends ESObject[Person] with CustomID
{
  def meta=Person
  def calcID(): Option[String] = Some(age.toString)

}

object Person extends ESMeta[Person] with ESReflect[Person] {
  
}

@ESDocument
case class DomainFragment(@NoIndex(false) subdomain:String, domain:String, tld:String)  extends ESObject[DomainFragment]{
  def meta = DomainFragment
  def fullDomain:String=s"http://$subdomain.$domain.$tld"
}

object DomainFragment extends ESMeta[DomainFragment] with ESReflect[DomainFragment]{
  
}

case class People(age: Int)
//, @Fk father: Option[Person] = None, @Fk children: List[Person] = Nil,
//                  now:DateTime = DateTime.now(), date:Option[DateTime] = None,
//                  name:Option[String] = None) // ,metadata:Map[String,Metadata] = Map.empty


@ESDocument
case class CrawlResult(url: String, status: Int, duration: Long, content: String, size: Long,
                       headers: Map[String, String], domain: DomainFragment, spiderDate: DateTime = new DateTime(),
                        domains:List[DomainFragment]=Nil)
  extends ESObject[CrawlResult] with CustomID
{

  def meta=CrawlResult

  def calcID(): Option[String] = Some(UUID.nameUUIDFromString(url))

}

object CrawlResult extends ESMeta[CrawlResult]with ESReflect[CrawlResult]{
  
}

@ESDocument
case class Role(name:String, permissions:List[Permission]=Nil) extends ESObject[Role] with CustomID {
  def meta = Role


  override def equals(other: Any): Boolean = other match {
    case r: Role => r._id == this._id
    case _ => false
  }

  def calcID(): Option[String] = Some(this.name)
}
object Role extends ESMeta[Role]  with ESReflect[Role] {
}

case class LoginCredentials(email: String, isRememberMe: Boolean = false)

@ESDocument
case class User(username:String, password:Password, name:String,   email:Email, locale:Locale="en_US", location:Option[String]=None, timezone:Option[TimeZone]=None,
                token:Option[String]=None, bio:Option[Text]=None, verified:Boolean=false, permissions:List[Permission]=Nil,
                @Fk roles:List[Role]=Nil) extends ESObject[User] with CustomID {
  def meta = User

  def calcID(): Option[String] = Some(username)
}

object User extends ESMeta[User] with ESReflect[User] {


}

object SystemUser {
  private val username = "admin"
  private val email = "admin"

}

@ESDocument
case class Permission(
                       val domain: String,
                       val actions: Set[String] = Set("*"),
                       val entities: Set[String] = Set("*")) extends ESObject[Permission]
{
  def meta = Permission

}

object Permission extends ESMeta[Permission] with ESReflect[Permission] {

  lazy val all = Permission("*")
  lazy val none = Permission("")
}
