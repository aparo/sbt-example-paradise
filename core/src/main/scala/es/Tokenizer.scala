package es

import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonDSL._

/** @author Alberto Paro */
abstract class Tokenizer(val name: String) {
  def build(source: JObject): JObject = source
  def customized: Boolean = false
}

case object WhitespaceTokenizer extends Tokenizer("whitespace")
