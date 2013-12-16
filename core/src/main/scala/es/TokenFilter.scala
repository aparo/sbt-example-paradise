package es

import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonDSL._

/** @author Alberto Paro */
abstract class TokenFilter(val name: String) {
  def build(source: JObject): JObject = source
  def customized = false
}

case object ReverseTokenFilter extends TokenFilter("reverse")
