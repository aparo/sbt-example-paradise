package es

import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

/** @author Alberto Paro */
abstract class Analyzer(val name: String)

case object NotAnalyzed extends Analyzer("notindexed")
case object WhitespaceAnalyzer extends Analyzer("whitespace")
case object StandardAnalyzer extends Analyzer("standard")
case object SimpleAnalyzer extends Analyzer("simple")
case object StopAnalyzer extends Analyzer("stop")
case object KeywordAnalyzer extends Analyzer("keyword")
case object PatternAnalyzer extends Analyzer("pattern")
case object SnowballAnalyzer extends Analyzer("snowball")
case class CustomAnalyzer(override val name: String) extends Analyzer(name)

abstract class LanguageAnalyzer(name: String) extends Analyzer(name: String)

case object ArabicLanguageAnalyzer extends LanguageAnalyzer("arabic")
case object ArmenianLanguageAnalyzer extends LanguageAnalyzer("armenian")
case object BasqueLanguageAnalyzer extends LanguageAnalyzer("basque")
case object BrazilianLanguageAnalyzer extends LanguageAnalyzer("brazilian")
case object BulgarianLanguageAnalyzer extends LanguageAnalyzer("bulgarian")
case object CatalanLanguageAnalyzer extends LanguageAnalyzer("catalan")
case object ChineseLanguageAnalyzer extends LanguageAnalyzer("chinese")
case object CjkLanguageAnalyzer extends LanguageAnalyzer("cjk")
case object CzechLanguageAnalyzer extends LanguageAnalyzer("czech")
case object DanishLanguageAnalyzer extends LanguageAnalyzer("danish")
case object DutchLanguageAnalyzer extends LanguageAnalyzer("dutch")
case object EnglishLanguageAnalyzer extends LanguageAnalyzer("english")
case object FinnishLanguageAnalyzer extends LanguageAnalyzer("finnish")
case object FrenchLanguageAnalyzer extends LanguageAnalyzer("french")
case object GalicianLanguageAnalyzer extends LanguageAnalyzer("galician")
case object GermanLanguageAnalyzer extends LanguageAnalyzer("german")
case object GreekLanguageAnalyzer extends LanguageAnalyzer("greek")
case object HindiLanguageAnalyzer extends LanguageAnalyzer("hindi")
case object HungarianLanguageAnalyzer extends LanguageAnalyzer("hungarian")
case object IndonesianLanguageAnalyzer extends LanguageAnalyzer("indonesian")
case object ItalianLanguageAnalyzer extends LanguageAnalyzer("italian")
case object LatvianLanguageAnalyzer extends LanguageAnalyzer("latvian")
case object NorwegianLanguageAnalyzer extends LanguageAnalyzer("norwegian")
case object PersianLanguageAnalyzer extends LanguageAnalyzer("persian")
case object PortugueseLanguageAnalyzer extends LanguageAnalyzer("portuguese")
case object RomanianLanguageAnalyzer extends LanguageAnalyzer("romanian")
case object RussianLanguageAnalyzer extends LanguageAnalyzer("russian")
case object SpanishLanguageAnalyzer extends LanguageAnalyzer("spanish")
case object SwedishLanguageAnalyzer extends LanguageAnalyzer("swedish")
case object TurkishLanguageAnalyzer extends LanguageAnalyzer("turkish")
case object ThaiLanguageAnalyzer extends LanguageAnalyzer("thai")

abstract class AnalyzerDefinition(val name: String) {
  def build(source: JObject): JObject
}

case class StopAnalyzerDefinition(override val name: String,
                                  stopwords: Iterable[String] = Nil,
                                  maxTokenLength: Int = 0) extends AnalyzerDefinition(name) {
  def build(source: JObject): JObject = source ~ ("type" -> "stop") ~ ("stopwords" -> stopwords)
}

case class StandardAnalyzerDefinition(override val name: String,
                                      stopwords: Iterable[String] = Nil,
                                      maxTokenLength: Int = 0) extends AnalyzerDefinition(name) {
  def build(source: JObject): JObject = source ~
    ("type" -> "standard") ~ ("max_token_length" -> maxTokenLength) ~ ("stopwords" -> stopwords)
}

case class PatternAnalyzerDefinition(override val name: String,
                                     regex: String,
                                     lowercase: Boolean = true) extends AnalyzerDefinition(name) {
  def build(source: JObject): JObject = source ~ ("type" -> "pattern") ~ ("lowercase" -> lowercase) ~
    ("pattern" -> regex)

}

case class SnowballAnalyzerDefinition(override val name: String,
                                      lang: String = "English",
                                      stopwords: Iterable[String] = Nil) extends AnalyzerDefinition(name) {
  def build(source: JObject): JObject = source ~ ("type" -> "snowball") ~ ("language" -> lang)
}

case class CustomAnalyzerDefinition(override val name: String,
                                    tokenizer: Tokenizer,
                                    filters: TokenFilter*) extends AnalyzerDefinition(name) {
  def build(source: JObject): JObject = source ~ ("type" -> "custom") ~ ("tokenizer" -> tokenizer.name) ~
    ("filter" -> filters.map(_.name))
}

abstract class LanguageAnalyzerDef(override val name: String,
                                   stopwords: Iterable[String] = Nil) extends AnalyzerDefinition(name) {
  def build(source: JObject): JObject = source ~ (name -> ("lang" -> name))
}
