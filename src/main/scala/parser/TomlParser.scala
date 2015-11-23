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
  this: TomlSymbol =>
  import Toml.NamedFunction
  val Whitespace = NamedFunction(WSChars.contains(_: Char), "Whitespace")
  val Digits = NamedFunction('0' to '9' contains (_: Char), "Digits")
  val Letters = NamedFunction((('a' to 'z') ++ ('A' to 'Z')).contains, "Letters")
  val UntilNewline = NamedFunction(!NLChars._1.contains(_: Char), "UntilNewline")
}

trait TomlSymbol {
  val SingleQuote = "\'"
  val DoubleQuote = "\""
  val Quotes = SingleQuote + DoubleQuote
  val Braces = ("[", "]")
  val Dashes = "-_"
  val NLChars = ("\r\n", "\n")
  val WSChars = " \t"
}

trait TomlParser extends ParserUtil with TomlSymbol {
  import fastparse.all._
  import Toml._

  val newline = P(StringIn(NLChars._1, NLChars._2))
  val charsChunk = P(CharsWhile(UntilNewline))
  val comment: P0 = P { "#" ~ charsChunk.rep ~ &(newline | End) }
  val WS0: P0 = P { CharsWhile(Whitespace) }
  val WS: P0 = P { NoCut(NoTrace((WS0 | comment | newline).rep.?)) }

  val letters = P { CharsWhile(Letters) }
  val digits = P { CharsWhile(Digits) }

  val literalChars = NamedFunction(!SingleQuote.contains(_: Char), "LitStr")
  val basicChars = NamedFunction(!DoubleQuote.contains(_: Char), "BasicStr")
  val unescapedChars = P { CharsWhile(literalChars) }
  val escapedChars = P { CharsWhile(basicChars) | "\\\""}

  val basicStr: Parser[Str] =
    P { DoubleQuote ~/ escapedChars.rep.! ~ DoubleQuote } map Str
  val literalStr: Parser[Str] =
    P { SingleQuote ~/ unescapedChars.rep.! ~ SingleQuote } map Str
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
    P { validKey ~ WS0.? ~ "=" ~ WS0.? ~ elem } map {
      kv: (String, Elem) => Pair(kv._1, kv._2)
    }

  lazy val array: Parser[Arr] =
    P { "[" ~ WS ~ elem.rep(sep=WS0.? ~ "," ~/ WS) ~ WS ~ "]" } map Arr

  val tableIds: Parser[Seq[String]] =
    P { validKey.rep(min=1, sep=WS0.? ~ "." ~ WS0.?) }
  val tableDef: Parser[Seq[String]] =
    P { "[" ~ WS0.? ~ tableIds ~ WS0.? ~ "]" }
  val table: Parser[LMap] =
    P { tableDef ~ WS ~ pair.rep(sep=WS) } map {
      t => LMap(t._1, t._2)
    }

  lazy val elem: Parser[Elem] = P {
    WS ~ (table | string | boolean | double | integer | array) ~ WS ~ End.?
  }
}

object TomlParser extends TomlParser