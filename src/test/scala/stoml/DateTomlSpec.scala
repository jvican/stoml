package stoml

import java.time._

import org.scalacheck.Gen
import org.scalatest.prop._
import org.scalatest.{FunSuite, Matchers, PropSpec}

trait DateTomlGen {
  import Gen.chooseNum

  def pad(n: Int, s: String): String =
    if(s.length < n) ("0" * (n - s.length)) + s else s
  def genNum(digits: Int, from: Int, to: Int): Gen[String] =
    chooseNum(from, to).map(n => pad(digits, n.toString))

  /* This check is not covering all the formats */
  val dateFormatGen: Gen[String] = for {
    day <- genNum(2, 1, 28)
    month <- genNum(2, 1, 12)
    year <- genNum(4, 0, 2200)
    hour <- genNum(2, 0, 23)
    minute <- genNum(2, 0, 59)
    second <- genNum(2, 0, 59)
    micro <- genNum(3, 0, 999)
  } yield year + "-" + month + "-" + day +
          "T" + hour + ":" + minute + ":" +
          second + "." + micro + "Z"
}

class DateTomlSpec extends PropSpec
    with PropertyChecks with Matchers 
    with DateTomlGen with TomlParser 
    with TestParserUtil {

  property("parse dates following the RFC 3339 spec (`date` parser)") {
    forAll(dateFormatGen) {
      s =>
        shouldBeSuccess(offsetDateTime.parse(s))
    }
  }

  property("parse dates following the RFC 3339 spec") {
    forAll(dateFormatGen) {
      s =>
        shouldBeSuccess(elem.parse(s))
    }
  }
}

class DataTomlUnitSpec extends FunSuite
  with PropertyChecks with Matchers
  with DateTomlGen with TomlParser
  with TestParserUtil {

  test("Parse local date") {
    val toml = "ld = 1979-05-27"
    val nodes = testSuccess(toml)
    assert(nodes(0) == Toml.Pair("ld", Toml.Date(LocalDate.of(1979, 5, 27))))
  }

  test("Parse local time") {
    val toml =
      """
        |lt1 = 07:32:00
        |lt2 = 00:32:00.999999
        |lt3 = 00:32:00.555
      """.stripMargin

    val nodes = testSuccess(toml)
    assert(nodes(0) == Toml.Pair("lt1", Toml.Time(LocalTime.of(7, 32, 0, 0))))
    assert(nodes(1) == Toml.Pair("lt2", Toml.Time(LocalTime.of(0, 32, 0, 999999000))))
    assert(nodes(2) == Toml.Pair("lt3", Toml.Time(LocalTime.of(0, 32, 0, 555000000))))
  }

  test("Parse local date time") {
    val toml =
      """
        |ldt1 = 1979-05-27T07:32:00
        |ldt2 = 1979-05-27T00:32:00.999999
      """.stripMargin
    val nodes = testSuccess(toml)
    assert(nodes(0) == Toml.Pair("ldt1", Toml.DateTime(LocalDateTime.of(
      LocalDate.of(1979, 5, 27), LocalTime.of(7, 32, 0, 0)))))
    assert(nodes(1) == Toml.Pair("ldt2", Toml.DateTime(LocalDateTime.of(
      LocalDate.of(1979, 5, 27), LocalTime.of(0, 32, 0, 999999000)))))
  }

  test("Parse offset date time") {
    val toml =
      """
        |odt1 = 1979-05-27T07:32:00Z
        |odt2 = 1979-05-27T00:32:00-07:00
        |odt3 = 1979-05-27T00:32:00.999999-07:00
      """.stripMargin
    val nodes = testSuccess(toml)
    assert(nodes(0) == Toml.Pair("odt1", Toml.OffsetDateTime(
      OffsetDateTime.of(
        LocalDateTime.of(
          LocalDate.of(1979, 5, 27), LocalTime.of(7, 32, 0)
        ), ZoneOffset.of("Z")))))
    assert(nodes(1) == Toml.Pair("odt2", Toml.OffsetDateTime(
      OffsetDateTime.of(
        LocalDateTime.of(
          LocalDate.of(1979, 5, 27), LocalTime.of(0, 32, 0)
        ), ZoneOffset.of("-07:00")))))
    assert(nodes(2) == Toml.Pair("odt3", Toml.OffsetDateTime(
      OffsetDateTime.of(
        LocalDateTime.of(
          LocalDate.of(1979, 5, 27), LocalTime.of(0, 32, 0, 999999000)
        ), ZoneOffset.of("-07:00")))))
  }
}
