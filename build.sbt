lazy val stoml = project.in(file("."))

name := "stoml"

organization := "me.vican.jorge"

scalaVersion := "2.12.0"

crossScalaVersions := Seq("2.10.6", "2.11.7", "2.12.0")

resolvers += Resolver.bintrayRepo("jvican", "releases")

libraryDependencies ++= Vector(
  "com.lihaoyi" %% "fastparse" % "0.4.2",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

licenses := Seq("MPL-2.0" -> url("https://opensource.org/licenses/MPL-2.0"))
pomExtra in Global := {
  <developers>
    <developer>
      <id>jvican</id>
      <name>Jorge Vicente Cantero</name>
      <url>https://github.com/jvican</url>
    </developer>
  </developers>
}

lazy val bla = settingKey[Boolean]("")
bla := {
  true
}

lazy val initExec = taskKey[Unit]("Eagerly initialize something.")
initExec := {
  // The bug describes that this should be run first. However, it's
  // not, `eagerInit.value` is executed before this statement.
  sys.error("I SHOULD HAVE BEEN RUN FIRST")
  if (bla.value) {
    eagerInit.value
  }
}
