package stoml

import fastparse.all._
import fastparse.core.Parsed.{Failure, Success}
import org.scalatest.Matchers

trait TestParserUtil {
  this: Matchers =>

  def testSuccess(example: String): Seq[Toml.Node] =
    TomlParserApi.nodes.parse(example) match {
      case Success(v, _)    => v
      case f: Failure[_, _] => fail(s"Failed to parse `$example`: ${f.msg}")
    }

  def shouldBeSuccess[T](r: Parsed[T]) = r match {
    case s: Success[T, _, _] =>
    case f: Failure[_, _] => fail(s"$r is not a Success.")
  }

  def shouldBeFailure[T](r: Parsed[T]) = r match {
    case s: Success[T, _, _] => fail(s"$r is not a Failure.")
    case f: Failure[_, _] =>
  }
}
