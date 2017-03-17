lazy val stoml = project.in(file("."))

name := "stoml"

organization := "me.vican.jorge"

scalaVersion := "2.11.7"
  
resolvers += Resolver.bintrayRepo("jvican", "releases")

libraryDependencies ++= Vector(
  "com.lihaoyi" %% "fastparse" % "0.3.7",
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
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
bintrayRepository := "nightlies"
releaseCrossBuild := false

/* Tricking the sbt-platform plugin to test `releaseStable`
 * This test is very brittle and will need to change soon.  */
platformCiEnvironment := {
  val rootDir =
    if (platformInsideCi.value) file("/drone")
    else file(System.getProperty("user.home"))
  val randomBuildNumber = scala.util.Random.nextInt.abs
  Some(
    CIEnvironment(
      rootDir,
      "linux/x86",
      RepositoryInfo("", "", "", "", "", "", "", false, true),
      CommitInfo("", "", "", "", "", AuthorInfo("", "", "")),
      BuildInfo(randomBuildNumber, "", "", "", "", "", "", "", randomBuildNumber - 1, ""),
      "",
      Some(-1),
      Some("v0.1")
    )
  )
}
