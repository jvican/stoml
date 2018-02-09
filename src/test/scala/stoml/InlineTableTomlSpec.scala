package stoml

import fastparse.core.Parsed.Success
import org.scalacheck.Gen
import org.scalatest.prop._
import org.scalatest.{Matchers, PropSpec}

trait InlineTableTomlGen {
  this: TomlSymbol
    with StringTomlGen
    with NumbersTomlGen =>

  val openChars = List("{", "{\n")
  val seps = List(",\n", ",")
  val closeChars = List("}", "\n}")

  def bareKeyGen = Gen.someOf(
    Gen.alphaLowerChar,
    Gen.alphaUpperChar,
    Gen.alphaNumChar,
    Gen.oneOf('_', '-')
  ).retryUntil(x => x.nonEmpty && !x.contains('=')).map(_.mkString)

  private def validPairGen: Gen[(String, String)] =
    for {
      key   <- bareKeyGen
      value <- Gen.oneOf(validStrGen, validDoubleGen, validLongGen)
    } yield (key, value)

  private def invalidPairGen: Gen[(String, String)] =
    for {
      key   <- Gen.someOf(bareKeyGen, "=").map(_.mkString)
      value <- Gen.oneOf("", ",")
    } yield (key, value)

  def tableFormat(s: Seq[(String, String)], fs: (String, String, String)): String =
    fs._1 +
    s.map { case (k, v) => k + "=" + v }.mkString(fs._2) +
    fs._3

  def inlineTableGen(pairGen: Gen[(String, String)]) =
    for {
      elems <- Gen.nonEmptyListOf(pairGen)
      c1 <- Gen.oneOf(openChars)
      ss <- Gen.oneOf(seps)
      c2 <- Gen.oneOf(closeChars)
    } yield tableFormat(elems, (c1, ss, c2))

  def validInlineTableGen  : Gen[String] = inlineTableGen(validPairGen)
  def invalidInlineTableGen: Gen[String] = inlineTableGen(invalidPairGen)
}

class InlineTableTomlSpec extends PropSpec
  with PropertyChecks
  with Matchers
  with InlineTableTomlGen
  with StringTomlGen
  with NumbersTomlGen
  with TomlParser
  with TestParserUtil {

  property("parse valid inline tables") {
    forAll(validInlineTableGen) {
      s: String =>
        shouldBeSuccess(elem.parse(s))
    }
  }

  property("do not parse invalid inline tables") {
    forAll(invalidInlineTableGen) {
      s: String =>
        shouldBeFailure(elem.parse(s))
    }
  }
}
