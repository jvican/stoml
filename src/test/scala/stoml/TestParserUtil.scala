package stoml

import fastparse.core.Parsed
import fastparse.core.Parsed.{Failure, Success}
import org.scalatest.Matchers

trait TestParserUtil {
  this: Matchers =>

  def shouldBeSuccess[T](r: Parsed[T]) = r match {
    case s: Success[T] => assert(true)
    case f: Failure => fail(s"$r is not a Success.")
  }

  def shouldBeFailure[T](r: Parsed[T]) = r match {
    case s: Success[T] => fail(s"$r is not a Failure.")
    case f: Failure => assert(true)
  }
}
