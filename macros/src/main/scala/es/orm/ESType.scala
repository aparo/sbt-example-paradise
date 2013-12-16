package es.orm
import scala.language.experimental.macros

import scala.annotation.StaticAnnotation
import scala.reflect.macros.Context

object typeNameMacro {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    import Flag._

//    annottees.map(_.tree).toList.foreach(a => println(showRaw(a)))

//    var result =
//      annottees.map(_.tree).toList.map {
//        _ match {
//          case ModuleDef(mods, name, Template(parents, self, body)) =>
//            moduleOk=true
//            cookModule(mods, name, parents, self, body)
//          case ClassDef(mods, name, types, Template(parents, self, body)) =>
//            className=newTermName(name.decoded)
//            typeName=name
//            cookClass(mods, name, types, parents, self, body)
//        }
//      }.toList.asInstanceOf[List[Tree]]
//    if(!moduleOk)
//      q"""object $className {}""" match {
//        case ModuleDef(mods, name, Template(parents, self, body)) =>
//          result ::= cookModule(mods, name, List(AppliedTypeTree(Select(Select(Ident(newTermName("es")), newTermName("macros")), newTypeName("ESObject")), List(Ident(typeName)))), self, body)
//      }
    val result=annottees.map(_.tree).toList
    c.Expr[Any](Block(result, Literal(Constant(()))))
  }

}


