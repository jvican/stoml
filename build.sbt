lazy val stoml = project.in(file("."))

name := "stoml"

organization := "me.vican.jorge"

scalaVersion := "2.12.0"

crossScalaVersions := Seq("2.10.6", "2.11.7", "2.12.0")

libraryDependencies ++= Vector(
  "com.lihaoyi" %% "fastparse" % "0.4.2",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

homepage := Some(url("https://github.com/jvican/stoml"))
licenses := Seq("MPL-2.0 License" -> url("https://opensource.org/licenses/MPL-2.0"))

pomExtra in Global := {
  <scm>
    <developerConnection>scm:git:git@github.com:jvican</developerConnection>
    <url>https://github.com/jvican/stoml.git</url>
    <connection>scm:git:git@github.com:jvican/stoml.git</connection>
  </scm>
  <developers>
    <developer>
      <id>jvican</id>
      <name>Jorge Vicente Cantero</name>
      <url>https://github.com/jvican</url>
    </developer>
  </developers>
}
