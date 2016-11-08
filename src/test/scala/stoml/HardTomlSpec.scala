package stoml

import java.io.File

import org.scalatest.{FlatSpec, Matchers}
import fastparse.core.Parsed.{Failure, Success}

class HardTomlSpec extends FlatSpec with Matchers {

  import stoml.TomlParserApi._

  def testParser(example: String) = {
    val parsed = parseToml(example)
    parsed match {
      case Success(v, _) =>
      case f: Failure[_, _] =>
        fail(s"Failed to parse `$example`: $f")
    }
  }

  def testFailingParser(example: String) = {
    val parsed = parseToml(example)
    parsed match {
      case Success(v, _) =>
        fail(s"Didn't fail to parse `$example`.")
      case f: Failure[_, _] =>
    }
  }

  "The TOML parser" should "be able to parse the hard TOML example" in {
    val parsed = parseToml(new File("./src/test/scala/stoml/hard_example.toml"))
    parsed match {
      case Success(v, _) =>
      case f: Failure[_, _] =>
        fail(s"The hard example failed with: $f")
    }
  }

  it should "parse escaped double quotes inside a string" in {
    val example =
      """
        |harder_test_string = " And when \"'s are in the string, along with # \""   # "and comments are there too"
      """.stripMargin
    testParser(example)
  }

  it should "parse complex table keys" in {
    val example =
      """[asdf."bit#"]
        |"hello" = "asdfasdf"
      """.stripMargin
    testParser(example)
  }

  it should "parse multi-line array with trailing commas" in {
    val example =
      """
        |multi_line_array = [
        |    "]",
        |    # ] Oh yes I did
        |    ]
      """.stripMargin
    testParser(example)
  }

  it should "fail to parse non-toml-compliant statement" in {
    val example = "[error]   if you didn't catch this, your parser is broken"
    testFailingParser(example)
  }

  it should "fail to parse comment at EOL" in {
    val example = "string = \"Anything other than tabs, spaces and newline after a keygroup or key value pair has ended should produce an error unless it is a comment\"   like this"
    testFailingParser(example)
  }

  it should "fail to parse end of comment after tricky array declaration" in {
    val example =
      """array = [
        |         "This might most likely happen in multiline arrays",
        |         Like here,
        |         "or here,
        |         and here"
        |]     End of array comment, forgot the #
      """.stripMargin
    testFailingParser(example)
  }

  it should "fail to parse comment at the end of key-pair definition" in {
    val example = "number = 3.14  pi <--again forgot the #"
    testFailingParser(example)
  }
}
