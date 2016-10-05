//val example = """harder_test_string = "And when \"'s are in the string, along with # \""   # "and comments are there too" """
import fastparse.all._
val example = """hard_test_string = "And when \"" """
import stoml.TomlParserApi._
parseToml(example)
basicStr.parse(""""And when \"'s are in"""")
(("\\" ~ "\"") ~ "hola").parse("""\"hola""")
(("\\" ~/ "\"") | escapedChars).rep.!.parse("""And when \"'s are in""")
(escapedChars).rep(sep="\\" ~ "\"").!.parse(""" \"'s are in""")
