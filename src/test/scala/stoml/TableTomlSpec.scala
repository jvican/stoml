package stoml

import org.scalacheck.Gen
import org.scalatest.prop._
import org.scalatest.{Matchers, PropSpec}

import scala.language.postfixOps


trait TableTomlGen {
  this: TomlSymbol
    with StringTomlGen
    with NumbersTomlGen
    with BooleanTomlGen
    with CommentTomlGen =>

  val sps = List(" ", "\t")

  private def repeat(s: String, n: Int): String = {
    @annotation.tailrec
    def go(acc: String, i: Int): String =
      if(i == 0) acc else go(acc + s, i - 1)
    go(s, n)
  }

  def pairFormat(l: String, r: String, sp: String) =
    l + sp + "=" + sp + r

  def idTableFormat(ids: Seq[_], fs: (String, String, String)) =
    fs._1 + (ids mkString fs._2) + fs._3

  def tableFormat(id: String, ps: Seq[String]) =
    id + "\n" + ps.mkString("\n")

  import Gen.{listOfN, nonEmptyListOf, oneOf, const, chooseNum}

  def valueGen: Gen[String] = oneOf(
    validStrGen,
    validDoubleGen,
    validLongGen,
    validBoolGen
  )

  def bareKeyGen = Gen.someOf(
    Gen.alphaLowerChar,
    Gen.alphaUpperChar,
    Gen.alphaNumChar,
    Gen.oneOf('_', '-')
  ) suchThat (_.nonEmpty) map (_.mkString)

  def pairGen: Gen[String] = for {
    key <- oneOf(doubleQuoteStrGen, bareKeyGen)
    value <- valueGen
    sp <- oneOf(sps)
    i <- chooseNum(0, 5)
  } yield pairFormat(key, value, repeat(sp, i))

  def pairWithCommentsGen: Gen[String] = for {
    p <- pairGen
    commInPreviousLine <- commentGen
    commInSameLine <- commentGen
  } yield commInPreviousLine + p + commInSameLine

  def tableDefGen: Gen[String] = for {
    labels <- nonEmptyListOf(doubleQuoteStrGen)
    sp <- oneOf(sps)
    i <- chooseNum(0, 10)
    sep <- const(repeat(sp, i) + "." + repeat(sp, i))
  } yield idTableFormat(labels, ("[", sep , "]"))

  def tableGen = for {
    tdef <- tableDefGen
    n <- chooseNum(0, 10)
    ps <- listOfN(n, pairWithCommentsGen)
    c1 <- commentGen
    c2 <- commentGen
  } yield c1 + tableFormat(tdef, ps) + c2

}

class TableTomlSpec extends PropSpec 
    with PropertyChecks with Matchers
    with BooleanTomlGen with StringTomlGen
    with NumbersTomlGen with TableTomlGen
    with CommentTomlGen with TomlParser
    with TestParserUtil {

  import Toml._

  property("parse pairs (key and value)") {
    forAll(pairGen) {
      s: String =>
        shouldBeSuccess[Pair](pair.parse(s))
    }
  }

  property("parse pairs (with `node` parser)") {
    forAll(pairGen) {
      s: String =>
        shouldBeSuccess(node.parse(s))
    }
  }

  property("parse table definitions") {
    forAll(tableDefGen) {
      s: String =>
        shouldBeSuccess[Seq[String]](tableDef.parse(s))
    }
  }

  property("parse tables") {
    forAll(tableGen) {
      s: String =>
        shouldBeSuccess[Table](table.parse(s))
    }
  }

  property("parse tables (with `node` parser)") {
    forAll(tableGen) {
      s: String =>
        shouldBeSuccess(node.parse(s))
    }
  }
}
