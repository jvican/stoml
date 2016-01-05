package stoml

import org.scalacheck.Gen

trait CommentTomlGen {
  this: TomlSymbol =>
  /* Generate comments in a low probability */
  def commentGen: Gen[String] = for {
    strChunk <- Gen.alphaStr
    rand <- Gen.chooseNum(1, 10)
    nl <- Gen.oneOf(NLChars._1, NLChars._2)
  } yield if(rand <= 3) CommentSymbol + strChunk + nl else ""
}
