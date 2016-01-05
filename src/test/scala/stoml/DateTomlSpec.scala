package stoml

import org.scalacheck.Gen
import org.scalatest.prop._
import org.scalatest.{Matchers, PropSpec}
import fastparse.core.Parsed.Success

trait DateTomlGen {
  import Gen.chooseNum

  def pad(n: Int, s: String): String =
    if(s.length < n) ("0" * (n - s.length)) + s else s
  def genNum(digits: Int, from: Int, to: Int): Gen[String] =
    chooseNum(from, to).map(n => pad(digits, n.toString))

  /* This check is not covering all the formats */
  val dateFormatGen: Gen[String] = for {
    day <- genNum(2, 0, 28)
    month <- genNum(2, 1, 12)
    year <- genNum(4, 0, 2200)
    hour <- genNum(2, 0, 23)
    minute <- genNum(2, 0, 59)
    second <- genNum(2, 0, 59)
    micro <- genNum(3, 0, 999)
  } yield (year + "-" + month + "-" + day +
           "T" + hour + ":" + minute + ":" + 
           second + "." + micro + "Z")
}

class DateTomlSpec extends PropSpec 
    with PropertyChecks with Matchers 
    with DateTomlGen with TomlParser 
    with TestParserUtil {

  property("parse dates following the RFC 3339 spec (`date` parser)") {
    forAll(dateFormatGen) {
      s =>
        shouldBeSuccess(date.parse(s))
    }
  }

  property("parse dates following the RFC 3339 spec") {
    forAll(dateFormatGen) {
      s =>
        shouldBeSuccess(elem.parse(s))
    }
  }
}
