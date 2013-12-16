package es.orm

import scala.reflect.macros.Context
import scala.language.experimental.macros
import es.utils.Mapper


trait ModelMappable


object ModelSerializers{

  import net.liftweb.json._
  import net.liftweb.json.Serialization

  implicit val formats = Serialization.formats(NoTypeHints) ++ net.liftweb.json.ext.JodaTimeSerializers.all

}


object ModelMappable {

  implicit class Mappable[M <: ModelMappable](val model: M) extends AnyVal {
    def asMap: Map[String, Any] = macro Macros.asMap_impl[M]
  }

  private object Macros {

    import scala.reflect.macros.Context

    def asMap_impl[T: c.WeakTypeTag](c: Context) = {
      import c.universe._

      val mapApply = Select(reify(Map).tree, newTermName("apply"))
      val model = Select(c.prefix.tree, newTermName("model"))

      val pairs = weakTypeOf[T].declarations.collect {
        case m: MethodSymbol if m.isCaseAccessor && m.name.decoded != "id" =>
          val name = c.literal(m.name.decoded)
          val value = c.Expr(Select(model, m.name))

          reify(name.splice -> value.splice).tree
      }
      //      println(pairs)
      c.Expr[Map[String, Any]](Apply(mapApply, pairs.toList))
    }
  }

}

trait Model{
  self =>
  def toMap=Mapper.toMap(self)

}

