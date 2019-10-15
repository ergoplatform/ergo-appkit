import scala.util.Try

name := "ergo-polyglot"

organization := "org.ergoplatform"

scalaVersion := "2.12.8"
version := "3.0.0"

resolvers ++= Seq(
  "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe maven releases" at "http://repo.typesafe.com/typesafe/maven-releases/"
)

val sigmaStateVersion = "exact-ops-ff7cc0e2-SNAPSHOT"
val ergoWalletVersion = "sigma-core-opt-fa221c39-SNAPSHOT"

libraryDependencies ++= Seq(
  ("org.scorexfoundation" %% "sigma-state" % sigmaStateVersion).force()
      .exclude("ch.qos.logback", "logback-classic")
      .exclude("org.scorexfoundation", "scrypto"),
  "org.ergoplatform" %% "ergo-wallet" % ergoWalletVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.+" % "test",
)

licenses in ThisBuild := Seq("CC0 1.0 Universal" -> url("https://github.com/ergoplatform/ergo-wallet/blob/master/LICENSE"))

homepage in ThisBuild := Some(url("https://github.com/aslesarenko/ergo-polyglot"))

publishMavenStyle in ThisBuild := true

publishArtifact in Test := false

publishTo in ThisBuild :=
  Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging)

pomExtra in ThisBuild :=
  <developers>
    <developer>
      <id>aslesarenko</id>
      <name>Alexander Slesarenko</name>
    </developer>
  </developers>

credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq

// set bytecode version to 8 to fix NoSuchMethodError for various ByteBuffer methods
// see https://github.com/eclipse/jetty.project/issues/3244
// these options applied only in "compile" task since scalac crashes on scaladoc compilation with "-release 8"
// see https://github.com/scala/community-builds/issues/796#issuecomment-423395500
//scalacOptions in(Compile, compile) ++= Seq("-release", "8")

assemblyJarName in assembly := s"ergo-polyglot-${version.value}.jar"

assemblyMergeStrategy in assembly := {
  case "logback.xml" => MergeStrategy.first
  case "module-info.class" => MergeStrategy.discard
  case other => (assemblyMergeStrategy in assembly).value(other)
}