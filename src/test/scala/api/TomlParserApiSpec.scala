package api

import fastparse.core.Parsed.{Failure, Success}
import org.scalatest.{FunSpec, Matchers}

class TomlParserApiSpec extends FunSpec with Matchers {
  import stoml.Toml._
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

    it("should parse parse table arrays") {
      val array =
        """
          |[[products]]
          |name = "Hammer"
          |sku = 738594937
          |colour = "blue"
          |
          |[[products]]
          |name = "Nail"
          |sku = 284758393
          |colour = "grey"
        """.stripMargin

      parseToml(array) match {
        case Success(v, _) =>
          val p = v.lookup("products")
          println(p)
          assert(p.contains(TableArrayItems(List(
            TableArray("products", List(
              Pair("name" -> Str("Hammer")),
              Pair("sku" -> Integer(738594937)),
              Pair("colour" -> Str("blue"))
            )),
            TableArray("products", List(
              Pair("name" -> Str("Nail")),
              Pair("sku" -> Integer(284758393)),
              Pair("colour" -> Str("grey"))
            ))
          ))))

        case f: Failure[_, _] => fail()
      }
    }
  }
}
