package stoml

import fastparse.core.Parsed.Success
import org.scalacheck.Gen
import org.scalatest.prop._
import org.scalatest.{Matchers, PropSpec}

trait BooleanTomlGen {
  this: TomlSymbol =>

  def toBool(s: String) = s match {
    case "true" => Toml.True
    case "false" => Toml.False
    case x => sys.error(s"$x is not either true or false.")
  }

  def validBoolGen: Gen[String] =
    Gen.oneOf("true", "false")

  def invalidBoolGen: Gen[String] =
    Gen.oneOf("True", "False")
}

class BooleanTomlSpec extends PropSpec 
    with PropertyChecks with Matchers
    with BooleanTomlGen with TomlParser
    with TestParserUtil {

  property("parse boolean literals") {
    forAll(validBoolGen) {
      s: String =>
        val expected = Success(toBool(s), s.length)
        elem.parse(s) shouldBe expected
    }
  }

  property("detect if boolean literals are not lowercase") {
    forAll(invalidBoolGen) {
      s: String =>
        shouldBeFailure(elem.parse(s))
    }
  }
}
