package parser

import scala.language.{implicitConversions, postfixOps}

object Toml {
  case class NamedFunction[T, V](f: T => V, name: String)
    extends (T => V) {
      def apply(t: T) = f(t)
      override def toString() = name
  }

  sealed trait Elem extends Any {
    def v: Any
  }

  sealed trait Bool extends Elem
  case object True extends Bool {def v = true}
  case object False extends Bool {def v = false}

  case class Comment(v: String) extends AnyVal with Elem
  case class Str(v: String) extends AnyVal with Elem
  case class Integ(v: Long) extends AnyVal with Elem
  case class Doub(v: Double) extends AnyVal with Elem
  case class Arr(v: Seq[Elem]) extends AnyVal with Elem
  case class Pair(v: (String, Elem)) extends AnyVal with Elem

  type Labels = Seq[String]
  case class LMap(v: (Labels, Map[String, Elem])) extends Elem
  object LMap {
    def apply(ls: Labels, ps: Seq[Pair]): LMap =
      LMap(ls -> (ps map (Pair.unapply(_).get) toMap))
  }
}

trait ParserUtil {
  import Toml.NamedFunction
  val Whitespace = NamedFunction(" \t".contains(_: Char), "Whitespace")
  val Digits = NamedFunction('0' to '9' contains (_: Char), "Digits")
  val Letters = NamedFunction((('a' to 'z') ++ ('A' to 'Z')).contains, "Letters")
  val UntilNewline = NamedFunction((_: Char) != '\n', "UntilNewline")
}

trait TomlSymbol {
  val SingleQuote = "\'"
  val DoubleQuote = "\""
  val Quotes = SingleQuote + DoubleQuote
  val Braces = ("[", "]")
  val Dashes = "-_"
}

trait TomlParser extends ParserUtil with TomlSymbol {
  import fastparse.all._
  import Toml._

  val space = P { CharsWhile(Whitespace).? }
  val spaceL = P { space ~ CharIn("\n").? }
  val letters = P { CharsWhile(Letters) }
  val digits = P { CharsWhile(Digits) }
  val comment = P { "#" ~ CharsWhile(UntilNewline, min=0) }.! map Comment

  val literalChars = NamedFunction(!SingleQuote.contains(_: Char), "LitStr")
  val basicChars = NamedFunction(!DoubleQuote.contains(_: Char), "BasicStr")
  val unescapedChars = P { CharsWhile(literalChars) }
  val escapedChars = P { CharsWhile(basicChars) | "\\\""}

  val basicStr: Parser[Str] =
    P { DoubleQuote ~! escapedChars.rep.! ~ DoubleQuote } map Str
  val literalStr: Parser[Str] =
    P { SingleQuote ~! unescapedChars.rep.! ~ SingleQuote } map Str
  val string: Parser[Str] = P { basicStr | literalStr }

  def rmUnderscore(s: String) = s.replace("_", "")
  val +- = P { CharIn("+-") }
  val integral = P { digits.rep(min=1, sep="_") }
  val fractional = P { "." ~ integral }
  val exponent = P { CharIn("eE") ~ +-.? ~ integral }
  val integer: Parser[Integ] =
    P { +-.? ~ integral }.! map (s => Integ(rmUnderscore(s).toLong))
  val double: Parser[Doub] =
    P { +-.? ~ integral ~ (fractional | exponent) }.! map {
      s => Doub(rmUnderscore(s).toDouble)
    }

  val `true` = P { "true" } map (_ => True)
  val `false` = P { "false" } map (_ => False)
  val boolean: Parser[Bool] = P { `true` | `false` }

  val dashes = P { CharIn(Dashes) }
  val bareKey = P { (letters | digits | dashes).rep(min=1) }
  val validKey = P { bareKey | NoCut(basicStr) }.!
  lazy val pair: Parser[Pair] =
    P { validKey ~ space ~ "=" ~ space ~ elem } map {
      kv: (String, Elem) => Pair(kv._1, kv._2)
    }

  lazy val array: Parser[Arr] =
    P { "[" ~ spaceL ~ elem.rep(sep=space ~ "," ~! spaceL) ~ spaceL ~ "]" } map Arr

  val tableIds: Parser[Seq[String]] =
    P { validKey.rep(min=1, sep=space ~ "." ~ space) }
  val tableDef: Parser[Seq[String]] =
    P { "[" ~ space ~ tableIds ~ space ~ "]" }
  val table: Parser[LMap] =
    P { tableDef ~ spaceL ~ pair.rep(sep=spaceL) } map {
      t => LMap(t._1, t._2)
    }

  lazy val elem: Parser[Elem] =
    P {
      space ~ (table | string | boolean | double | integer | array | comment) ~ space
    }
}

object TomlParser extends TomlParser
