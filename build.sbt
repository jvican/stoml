lazy val stoml = project.in(file("."))

name := "stoml"

organization := "me.vican.jorge"

scalaVersion := "2.12.0"

resolvers += Resolver.bintrayRepo("jvican", "releases")

crossScalaVersions := Seq("2.10.6", "2.11.7", "2.12.0")

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

// Publish to the user, no default organization
bintrayOrganization := None
releaseCrossBuild := false

/* Tricking the sbt-platform plugin to test `platformReleaseStable`
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
      BuildInfo(randomBuildNumber, "", "", "", "", "", "", None, None, None),
      "",
      Some(-1),
      Some("v0.1")
    )
  )
}
