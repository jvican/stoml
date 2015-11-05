name := "TOML Parser in Scala"

organization := "com.github.jvican"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Vector(
  "com.lihaoyi" %% "scalaparse" % "0.2.1",
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/MIT"))
