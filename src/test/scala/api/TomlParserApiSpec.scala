package api

import fastparse.core.Parsed.{Failure, Success}
import org.scalatest.{FunSpec, Matchers}

class TomlParserApiSpec extends FunSpec with Matchers {
  import stoml.TomlParserApi._

  val smallFileTest =
    """best-author-ever = "Anonymous"
      |
      |[num."theory"]
      |boring = false
      |perfection = [6, 28, 496]""".stripMargin

  describe("The TomlParser API") {
    it("should allow lookups in parsed content") {
      parseToml(smallFileTest) match {
        case Success(v, _) =>
          (v lookup "num.theory") should not be empty
          (v lookup "best-author-ever") should not be empty
        case f: Failure[_, _] =>
          fail("`toToml` has not parsed correctly the file")
      }
    }

    it("should allow filtering in parsed content") {
      parseToml(smallFileTest) match {
        case Success(v, _) =>
          v.filter(_.contains("e")).size shouldBe 2
        case f: Failure[_, _] =>
          fail("`toToml` has not parsed correctly the file")
      }
    }

    it("should allow filtering by the prefix key") {
      parseToml(smallFileTest) match {
        case Success(v, _) =>
          val subkeys = v.childOf("num").toVector
          subkeys.size shouldBe 1
          assert(subkeys(0).elem.isInstanceOf[(Any, Any)])
        case f: Failure[_, _] =>
          fail("`toToml` has not parsed correctly the file")
      }
    }
  }
}
