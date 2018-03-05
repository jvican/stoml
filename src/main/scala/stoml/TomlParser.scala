package stoml

import java.time.{
  LocalDateTime  => JLocalDateTime,
  LocalTime      => JLocalTime,
  LocalDate      => JLocalDate,
  OffsetDateTime => JOffsetDateTime,
  ZoneOffset
}

import java.io.File

import fastparse.all._

import scala.language.{implicitConversions, postfixOps}

private[stoml] trait Common {
  type Key = String
}

object Toml extends TomlSymbol with Common {

  case class NamedFunction[T, V](f: T => V, name: String) extends (T => V) {
    def apply(t: T) = f(t)

    override def toString() = name
  }

  sealed trait Elem extends Any {
    def elem: Any
  }

  sealed trait Node extends Any with Elem

  sealed trait Bool extends Elem

  case object True extends Bool {
    def elem = true
  }

  case object False extends Bool {
    def elem = false
  }

  case class Str(elem: String) extends AnyVal with Elem

  object Str {
    def dequoteStr(s: String, q: String) =
      s.stripPrefix(q).stripSuffix(q)

    def cleanStr(s: String): String =
      dequoteStr(dequoteStr(s, SingleQuote), DoubleQuote)

    def cleanedApply(s: String): Str = Str(cleanStr(s))
  }

  case class Integer(elem: Long) extends AnyVal with Elem

  case class Real(elem: Double) extends AnyVal with Elem

  case class Arr(elem: Seq[Elem]) extends AnyVal with Elem

  case class Date          (elem: JLocalDate     ) extends AnyVal with Elem
  case class Time          (elem: JLocalTime     ) extends AnyVal with Elem
  case class DateTime      (elem: JLocalDateTime ) extends AnyVal with Elem
  case class OffsetDateTime(elem: JOffsetDateTime) extends AnyVal with Elem

  case class Pair(elem: (String, Elem)) extends AnyVal with Node

  case class Table(elem: (Key, Map[String, Elem])) extends Node

  object Table {
    def apply(ls: Key, ps: Seq[Pair]): Table =
      Table(ls -> (ps map (Pair.unapply(_).get) toMap))
  }

  case class TableArray(elem: (Key, Map[String, Elem])) extends Node

  object TableArray {
    def apply(ls: Key, ps: Seq[Pair]): TableArray =
      TableArray(ls -> (ps map (Pair.unapply(_).get) toMap))
  }

  case class TableArrayItems(elem: List[TableArray]) extends Node
}

trait ParserUtil { this: TomlSymbol =>

  import Toml.NamedFunction

  val Whitespace = NamedFunction(WSChars.contains(_: Char), "Whitespace")
  val Letters =
    NamedFunction((('a' to 'z') ++ ('A' to 'Z')).contains(_: Char), "Letters")
  val UntilNewline =
    NamedFunction(!NLChars._1.contains(_: Char), "UntilNewline")
}

trait TomlSymbol {
  val SingleQuote = "\'"
  val DoubleQuote = "\""
  val Quotes = SingleQuote + DoubleQuote
  val Braces = ("[", "]")
  val Dashes = "-_"
  val NLChars = ("\r\n", "\n")
  val WSChars = " \t"
  val CommentSymbol = "#"
}

trait TomlParser extends ParserUtil with TomlSymbol {

  import Toml._

  val newline = P(StringIn(NLChars._1, NLChars._2))
  val charsChunk = P(CharsWhile(UntilNewline))
  val comment: P0 = P(CommentSymbol ~ charsChunk.rep ~ &(newline | End))
  val WS0: P0 = P(CharsWhile(Whitespace))
  val WS: P0 = P(NoCut(NoTrace((WS0 | comment | newline).rep.?)))

  val letters = P(CharsWhile(Letters))
  val digit = P(CharIn('0' to '9'))
  val digits = P(digit.rep(1))

  val skipEscapedDoubleQuote = P("\\" ~ "\"")
  val literalChars = NamedFunction(!SingleQuote.contains(_: Char), "LitStr")
  val basicChars = NamedFunction(!DoubleQuote.contains(_: Char), "BasicStr")
  val complexBasicChars = NamedFunction((_: Char) != 92.toChar, "BasicStr")
  val stoppingChars =
    NamedFunction(!s"$DoubleQuote\\".contains(_: Char), "StoppingChars")

  val untilEscapedOrEnd = CharsWhile(stoppingChars)
  val consumeUntilEnd = CharsWhile(basicChars)
  val unescapedChars = P(CharsWhile(literalChars))
  val escapedChars = P(
    untilEscapedOrEnd | skipEscapedDoubleQuote | consumeUntilEnd)

  val basicStr: Parser[Str] =
    P(DoubleQuote ~/ escapedChars.rep.! ~ DoubleQuote) map Str.cleanedApply
  val literalStr: Parser[Str] =
    P(SingleQuote ~/ unescapedChars.rep.! ~ SingleQuote) map Str.cleanedApply
  val string: Parser[Str] = P(basicStr | literalStr)

  def rmUnderscore(s: String) = s.replace("_", "")

  val +- = P {
    CharIn("+-")
  }
  val integral = P(digits.rep(min = 1, sep = "_"))
  val fractional = P("." ~ integral)
  val exponent = P(CharIn("eE") ~ +-.? ~ integral)
  val integer: Parser[Integer] =
    P(+-.? ~ integral).!.map(s => Integer(rmUnderscore(s).toLong))
  val double: Parser[Real] =
    P(+-.? ~ integral ~ (fractional | exponent)).!.map(s =>
      Real(rmUnderscore(s).toDouble))

  val `true` = P("true").map(_ => True)
  val `false` = P("false").map(_ => False)
  val boolean: Parser[Bool] = P(`true` | `false`)

  private val TenPowers =
    List(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)

  val localTime: Parser[Time] = P(
    digit.rep(2).! ~ ":" ~ digit.rep(2).! ~ ":" ~ digit.rep(2).! ~ ("." ~ digit.rep.!).?
  ).map { case (h, m, s, ns) =>
    val nano = ns.map { str =>
      val digits = str.length
      str.toInt * TenPowers(9 - digits)
    }.getOrElse(0)

    Time(JLocalTime.of(h.toInt, m.toInt, s.toInt, nano))
  }

  val localDate: Parser[Date] = P(
    digit.rep(4).! ~ "-" ~ digit.rep(2).! ~ "-" ~ digit.rep(2).!
  ).map { case (y, m, d) =>
    Date(JLocalDate.of(y.toInt, m.toInt, d.toInt))
  }

  val localDateTime: Parser[DateTime] = P(
    localDate ~ "T" ~ localTime
  ).map { case (date, time) =>
    DateTime(JLocalDateTime.of(date.elem, time.elem))
  }

  val offsetDateTime: Parser[OffsetDateTime] = P(
    localDateTime ~ ("Z" | (("-" | "+") ~ digit.rep(2) ~ ":" ~ digit.rep(2))).!
  ).map { case (dateTime, offset) =>
    OffsetDateTime(
      JOffsetDateTime.of(dateTime.elem, ZoneOffset.of(offset)))
  }

  val date = P(offsetDateTime | localDateTime | localDate | localTime)

  val dashes = P(CharIn(Dashes))
  val bareKey = P((letters | digits | dashes).rep(min = 1)).!
  val validKey: Parser[String] = P(bareKey | NoCut(basicStr)).!
  val pair: Parser[Pair] =
    P(validKey ~ WS0.? ~ "=" ~ WS0.? ~ elem) map Pair
  val array: Parser[Arr] =
    P("[" ~ WS ~ elem.rep(sep = "," ~ WS) ~ ",".? ~ WS ~ "]") map Arr
  val inlineTable: Parser[Table] =
    P("{" ~ WS ~ pair.rep(sep = "," ~ WS) ~ "}").map(p => Table("", p))

  val tableIds: Parser[Seq[String]] =
    P(validKey.rep(min = 1, sep = WS0.? ~ "." ~ WS0.?))
  val tableDef: Parser[Seq[String]] =
    P("[" ~ WS0.? ~ tableIds ~ WS0.? ~ "]")
  val tableArrayDef: Parser[Seq[String]] =
    P("[[" ~ WS.? ~ tableIds ~ WS.? ~ "]]")

  val table: Parser[Table] =
    P(WS ~ tableDef ~ WS ~ pair.rep(sep = WS)).map { case (a, b) =>
      Table(a.map(Str.cleanStr).mkString("."), b)
    }
  val tableArray: Parser[TableArray] =
    P(WS ~ tableArrayDef ~ WS ~ pair.rep(sep = WS)).map { case (a, b) =>
      TableArray(a.map(Str.cleanStr).mkString("."), b)
    }

  lazy val elem: Parser[Elem] = P {
    WS ~ (date | string | boolean | double | integer | array | inlineTable) ~ WS
  }

  lazy val node: Parser[Node] = P(WS ~ (pair | table | tableArray) ~ WS)
  lazy val nodes: Parser[Seq[Node]] = P(node.rep(min = 1, sep = WS) ~ End)
}

trait TomlParserApi extends TomlParser with Common {

  import stoml.Toml.{Node, Table, Pair, TableArray, TableArrayItems}

  case class TomlContent(map: Map[Key, Toml.Elem]) {
    def lookup(k: Key): Option[Toml.Elem] = map.get(k)
    def filter(f: Key => Boolean): Iterator[Toml.Elem] =
      map.filterKeys(f).values.iterator
    def childOf(parentKey: Key): Iterator[Toml.Elem] =
      filter(key => key.startsWith(parentKey))
  }

  object TomlContent {
    def apply(s: Seq[Node]): TomlContent = TomlContent {
      s.foldLeft(Map.empty[Key, Toml.Elem]) { (m, e) =>
        val value = e match {
          case t: Table => t.elem._1 -> t
          case t: TableArray =>
             t.elem._1 -> (m.get(t.elem._1) match {
              case Some(TableArrayItems(items)) =>
                TableArrayItems(items :+ t)
              case _ => TableArrayItems(List(t))
            })
          case p: Pair => p.elem._1 -> p.elem._2
        }

        m + value
      }
    }
  }

  def parseToml(s: String): Parsed[TomlContent] =
    (nodes map TomlContent.apply).parse(s)

  // Inefficient but necessary to parse everything from a String
  def parseToml(f: File): Parsed[TomlContent] =
    parseToml(scala.io.Source.fromFile(f).mkString)
}

object TomlParserApi extends TomlParserApi
