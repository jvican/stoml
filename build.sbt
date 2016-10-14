lazy val stoml = project.in(file("."))

name := "stoml"

organization := "me.vican.jorge"

scalaVersion := "2.11.7"

libraryDependencies ++= Vector(
  "com.lihaoyi" %% "fastparse" % "0.3.7",
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)

publishMavenStyle := false
homepage := Some(url("https://github.com/jvican/stoml"))
licenses := Seq("MPLv2 License" -> url("https://opensource.org/licenses/MPL-2.0"))

// Bintray
bintrayOrganization := None
bintrayRepository := "releases"
bintrayPackage := "stoml"
bintrayReleaseOnPublish := false

// Release
import ReleaseTransformations._
releasePublishArtifactsAction := PgpKeys.publishSigned.value
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  releaseStepTask(bintrayRelease in stoml),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
