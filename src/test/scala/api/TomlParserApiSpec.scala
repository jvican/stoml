package api

import fastparse.core.Parsed.{Failure, Success}
import org.scalatest.{Matchers, FunSpec}

class TomlParserApiSpec extends FunSpec with Matchers {
  import stoml.TomlParserApi._

  val smallFileTest =
    """best-author-ever = "Anonymous"
      |
      |[num."theory"]
      |boring = false
      |perfection = [6, 28, 496]""".stripMargin

  describe("The TomlParser API") {
    it("should parse a file correctly, parsing also the EOF") {
      toToml(smallFileTest) match {
        case Success(v, _) =>
          (v lookup Vector("num", "theory")) should not be empty
          (v lookup Vector("best-author-ever")) should not be empty
        case f: Failure =>
          fail("`toToml` has not parsed correctly the file")
      }
    }
  }
}
