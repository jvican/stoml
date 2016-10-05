package stoml

import java.io.File

import org.scalatest.{FlatSpec, Matchers}
import fastparse.core.Parsed.{Failure, Success}

class HardToml extends FlatSpec with Matchers {

  import stoml.TomlParserApi._

  "The TOML parser" should "be able to parse the hard TOML example" in {
    val parsed = parseToml(new File("./src/test/scala/stoml/hard_example.toml"))
    parsed match {
      case Success(v, _) =>
      case f: Failure =>
        fail(s"The hard example failed with: $f")
    }
  }

}
