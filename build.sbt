import scala.util.Try

version := "3.0.0"

lazy val commonSettings = Seq(
  organization := "org.ergoplatform",
  scalaVersion := "2.12.8",
  version := "3.0.0",
  //  resolvers ++= Seq("Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  //    "SonaType" at "https://oss.sonatype.org/content/groups/public",
  //    "Typesafe maven releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
  //    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"),
  homepage := Some(url("http://ergoplatform.org/")),
  licenses := Seq("CC0" -> url("https://creativecommons.org/publicdomain/zero/1.0/legalcode"))
)


resolvers ++= Seq(
  "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe maven releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
  Resolver.mavenCentral,
  Resolver.mavenLocal
)

val sigmaStateVersion = "exact-ops-ff7cc0e2-SNAPSHOT"
val ergoWalletVersion = "sigma-core-opt-fa221c39-SNAPSHOT"

libraryDependencies ++= Seq(
  ("org.scorexfoundation" %% "sigma-state" % sigmaStateVersion).force()
      .exclude("ch.qos.logback", "logback-classic")
      .exclude("org.scorexfoundation", "scrypto"),
  "org.ergoplatform" %% "ergo-wallet" % ergoWalletVersion,
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.+" % "test",
  "org.graalvm" % "graal-sdk" % "0.30",
  "com.squareup.retrofit2" % "retrofit" % "2.6.2",
  "org.jetbrains.kotlin" % "kotlin-stdlib" % "1.3.50",
  "org.jetbrains.kotlinx" % "kotlinx-coroutines-core" % "1.3.2",
  "com.squareup.retrofit2" % "converter-scalars" % "2.6.0",
  "com.squareup.retrofit2" % "converter-gson" % "2.6.0",
  "com.squareup.okhttp3" % "mockwebserver" % "4.2.1" % "test"
)

licenses in ThisBuild := Seq("CC0 1.0 Universal" -> url("https://github.com/ergoplatform/ergo-wallet/blob/master/LICENSE"))

homepage in ThisBuild := Some(url("https://github.com/aslesarenko/ergo-polyglot"))

publishMavenStyle in ThisBuild := true

publishArtifact in Test := true

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

lazy val allConfigDependency = "compile->compile;test->test"

lazy val javaClientGenerated = (project in file("java-client-generated"))
    .settings(
      commonSettings,
      name := "java-client-generated",
      libraryDependencies ++= Seq(
        "io.gsonfire" % "gson-fire" % "1.8.0" % "compile",
        "io.swagger.core.v3" % "swagger-annotations" % "2.0.0",
        "org.threeten" % "threetenbp" % "1.3.5" % "compile",
        "com.squareup.retrofit2" % "converter-scalars" % "2.6.0",
        "com.squareup.retrofit2" % "converter-gson" % "2.6.0",
        "org.apache.oltu.oauth2" % "org.apache.oltu.oauth2.client" % "1.0.2",
        "junit" % "junit" % "4.12" % "test",
      )
    )

lazy val libApi = (project in file("lib-api"))
    .settings(
      commonSettings,
      name := "lib-api",
      libraryDependencies ++= Seq(
      )
    )

lazy val libImpl = (project in file("lib-impl"))
    .dependsOn(libApi % allConfigDependency, javaClientGenerated % allConfigDependency)
    .settings(
      commonSettings,
      name := "lib-impl",
      libraryDependencies ++= Seq(
      )
    )
    
lazy val examples = (project in file("examples"))
    .dependsOn(
      libApi % allConfigDependency, 
      javaClientGenerated % allConfigDependency,
      libImpl % allConfigDependency)
    .settings(
      commonSettings,
      name := "examples",
      libraryDependencies ++= Seq(
      )
    )

lazy val ergoPolyglot = (project in file("."))
    .settings(commonSettings, name := "ergo-polyglot")
    .dependsOn(
      javaClientGenerated % allConfigDependency, 
      libApi % allConfigDependency, 
      libImpl % allConfigDependency
    )
