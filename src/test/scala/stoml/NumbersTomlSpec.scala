package stoml

import fastparse.core.Parsed.Success
import org.scalacheck.Gen
import org.scalatest.prop._
import org.scalatest.{Matchers, PropSpec}

trait NumbersTomlGen {
  val signChars = List("+", "-", "")

  def intersperse(x: String, sep: String) =
    x.toCharArray.mkString(sep)

  def validLongGen: Gen[String] = for {
    l <- Gen.posNum[Long]
    sign <- Gen.oneOf(signChars)
    us <- Gen.oneOf(List("", "_"))
  } yield sign + intersperse(l.toString, us)

  def validDoubleGen: Gen[String] = for {
      sign <- Gen.oneOf(signChars)
      d <- Gen.posNum[Double]
      l <- validLongGen
      l2 <- validLongGen
      e <- Gen.oneOf("e", "E")
      fs <- Gen.oneOf(sign + d.toString, l + e + l2)
    } yield fs
}

class NumbersTomlSpec extends PropSpec 
    with PropertyChecks with Matchers
    with NumbersTomlGen with TomlParser
    with TestParserUtil {

  import Toml._
  property("parse integers") {
    forAll(validLongGen) {
      s: String =>
        val expected = Success(Integer(rmUnderscore(s).toLong), s.length)
        elem.parse(s) shouldBe expected
    }
  }

  property("parse doubles") {
    forAll(validDoubleGen) {
      s: String =>
        val expected = Success(Real(rmUnderscore(s).toDouble), s.length)
        elem.parse(s) shouldBe expected
    }
  }
}
