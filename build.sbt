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

// Bintray
bintrayOrganization := None
releaseCrossBuild := false

lazy val releaseOnMergeOnlyCi = taskKey[Unit]("Release on merge only in CI.")
releaseOnMergeOnlyCi := {
  if (platformInsideCi.value)
    platformReleaseOnMerge.value
  else ()
}
