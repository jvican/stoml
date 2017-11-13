package stoml

import org.scalacheck.Gen
import org.scalatest.prop._
import org.scalatest.{Matchers, PropSpec}

import scala.language.postfixOps

trait ArrayTomlGen {
  this: TomlSymbol
    with StringTomlGen
    with NumbersTomlGen =>

  def arrayFormat(s: Seq[_], fs: (String, String, String)): String =
    fs._1 + (s mkString fs._2) + fs._3

  import Gen.{nonEmptyListOf, oneOf}

  def arrayGen(openChars: List[String],
               seps: List[String],
               closeChars: List[String]) = for {
    ts <- oneOf(validStrGen, validDoubleGen, validLongGen)
    elems <- nonEmptyListOf(ts)
    c1 <- oneOf(openChars)
    ss <- oneOf(seps)
    c2 <- oneOf(closeChars)
  } yield arrayFormat(elems, (c1, ss, c2))
}

class ArrayTomlSpec extends PropSpec 
    with PropertyChecks with Matchers
    with NumbersTomlGen with StringTomlGen
    with ArrayTomlGen with TomlParser
    with TestParserUtil {

  property("parse arrays") {
    val openChars = List("[", "[\n")
    val seps = List(",\n", ",")
    val closeChars = List("]", "\n]")

    forAll(arrayGen(openChars, seps, closeChars)) {
      s: String =>
        shouldBeSuccess(elem.parse(s))
    }
  }

  property("parse table arrays") {
    val openChars = List("[[", "[[\n")
    val seps = List(",\n", ",")
    val closeChars = List("]]", "\n]]")

    forAll(arrayGen(openChars, seps, closeChars)) {
      s: String =>
        shouldBeSuccess(elem.parse(s))
    }
  }
}
