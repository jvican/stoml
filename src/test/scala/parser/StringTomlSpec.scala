package parser

import fastparse.core.Result.Success
import org.scalacheck.Gen
import org.scalatest.prop._
import org.scalatest.{Matchers, PropSpec}

trait StringTomlGen {
  this: TomlSymbol =>

  def enquoteStr(s: String, q: String): String =
    q + s + q

  def dequoteStr(s: String, q: String): String =
    s.stripPrefix(q).stripSuffix(q)

  def dequote(s: String): String = s match {
    case x if x.startsWith(SingleQuote) && x.endsWith(SingleQuote) =>
      dequoteStr(x, SingleQuote)
    case x if x.startsWith(DoubleQuote) && x.endsWith(DoubleQuote) =>
      dequoteStr(x, DoubleQuote)
    case x => x
  }


  def quotedStrGen(quote: String): Gen[String] = for {
    s <- Gen.alphaStr
    if s != ""
  } yield enquoteStr(s filter (_ != quote), quote)

  def doubleQuoteStrGen: Gen[String] = quotedStrGen(DoubleQuote)
  def singleQuoteStrGen: Gen[String] = quotedStrGen(SingleQuote)
  def validStrGen: Gen[String] =
    Gen.oneOf(doubleQuoteStrGen, singleQuoteStrGen) filter (_ != "")

  def invalidStrGen: Gen[String] = for {
      s <- Gen.alphaStr
      f <- Gen.oneOf(List(
        SingleQuote + s,
        s + SingleQuote,
        DoubleQuote + s,
        s + DoubleQuote
      ))
    } yield f
}

class StringTomlSpec extends PropSpec with PropertyChecks with Matchers
                                        with StringTomlGen with TomlParser
                                        with TestParserUtil {
  import Toml._
  property("parse single and double-quoted strings") {
    forAll(validStrGen) {
      s: String =>
        val expected = Success(Str(dequote(s)), s.length)
        elem.parse(s) shouldBe expected
    }
  }

  property("detect if any string is unbalanced (it misses a quote)") {
    forAll(invalidStrGen) {
      s: String =>
        shouldBeFailure(elem.parse(s))
    }
  }
}
