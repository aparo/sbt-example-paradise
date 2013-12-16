package macrocc

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

object helloMacroCC {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    import Flag._
    val result = {
      annottees.map(_.tree).toList.map {
        case ModuleDef(mods, name, Template(parents, self, body)) =>
          ModuleDef(mods, name, Template(parents, self, body))
        case ClassDef(mods, name, types, Template(parents, self, body)) =>
          ClassDef(mods, name, types, Template(parents, self, body))
      }
    }.asInstanceOf[List[Tree]]

    c.Expr[Any](Block(result, Literal(Constant(()))))
  }
}

class helloCC(a:Int=0) extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro helloMacroCC.impl
}
