import sbt.Keys.publishMavenStyle
// first two digits of the version should be in sync with Ergo client
version := "3.1.0"

name := "ergo-appkit"

lazy val sonatypePublic = "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/"
lazy val sonatypeReleases = "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
lazy val sonatypeSnapshots = "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

lazy val commonSettings = Seq(
  organization := "org.ergoplatform",
  scalaVersion := "2.12.8",
  version := "3.1.0",
  resolvers ++= Seq(sonatypeReleases,
    "SonaType" at "https://oss.sonatype.org/content/groups/public",
    "Typesafe maven releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
    sonatypeSnapshots,
    Resolver.mavenCentral),
  homepage := Some(url("https://github.com/aslesarenko/ergo-appkit")),
  licenses := Seq("CC0" -> url("https://creativecommons.org/publicdomain/zero/1.0/legalcode")),
  pomExtra :=
      <developers>
        <developer>
          <id>aslesarenko</id>
          <name>Alexander Slesarenko</name>
          <url>https://github.com/aslesarenko/</url>
        </developer>
      </developers>,
  publishArtifact in (Compile, packageSrc) := true,
  publishArtifact in (Compile, packageDoc) := false,
  publishMavenStyle := true,
  publishTo := sonatypePublishToBundle.value,
)

val testingDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.+" % "test",
)

lazy val testSettings = Seq(
  libraryDependencies ++= testingDependencies,
  parallelExecution in Test := false,
  baseDirectory in Test := file("."),
  publishArtifact in Test := true,
  publishArtifact in(Test, packageSrc) := true,
  publishArtifact in(Test, packageDoc) := false,
  test in assembly := {})


lazy val allResolvers = Seq(
  sonatypePublic,
  sonatypeReleases,
  sonatypeSnapshots,
  Resolver.mavenCentral,
)

publishArtifact in Compile := true
publishArtifact in Test := true

publishTo in ThisBuild :=
    Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging)

credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq

// set bytecode version to 8 to fix NoSuchMethodError for various ByteBuffer methods
// see https://github.com/eclipse/jetty.project/issues/3244
// these options applied only in "compile" task since scalac crashes on scaladoc compilation with "-release 8"
// see https://github.com/scala/community-builds/issues/796#issuecomment-423395500
//scalacOptions in(Compile, compile) ++= Seq("-release", "8")
assemblyJarName in assembly := s"ergo-appkit-${version.value}.jar"

assemblyMergeStrategy in assembly := {
  case "logback.xml" => MergeStrategy.first
  case "module-info.class" => MergeStrategy.discard
  case other => (assemblyMergeStrategy in assembly).value(other)
}

lazy val allConfigDependency = "compile->compile;test->test"

val sigmaStateVersion = "3.1.0"
val ergoWalletVersion = "master-7921c215-SNAPSHOT"

lazy val sigmaState = ("org.scorexfoundation" %% "sigma-state" % sigmaStateVersion).force()
    .exclude("ch.qos.logback", "logback-classic")
    .exclude("org.scorexfoundation", "scrypto")

lazy val ergoWallet = "org.ergoplatform" %% "ergo-wallet" % ergoWalletVersion

libraryDependencies ++= Seq(
  sigmaState,
  ergoWallet,
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.+" % "test",
  "org.graalvm.sdk" % "graal-sdk" % "19.2.0.1",
  "org.jetbrains.kotlin" % "kotlin-stdlib" % "1.3.50",
  "org.jetbrains.kotlinx" % "kotlinx-coroutines-core" % "1.3.2",
  "com.squareup.retrofit2" % "retrofit" % "2.6.2",
  "com.squareup.retrofit2" % "converter-scalars" % "2.6.2",
  "com.squareup.retrofit2" % "converter-gson" % "2.6.2",
  "com.squareup.okhttp3" % "mockwebserver" % "3.12.0"
)

lazy val javaClientGenerated = (project in file("java-client-generated"))
    .settings(
      commonSettings,
      name := "java-client-generated",
      crossPaths := false,
      libraryDependencies ++= Seq(
        "com.squareup.okhttp3" % "okhttp" % "3.12.0",
        "com.google.code.findbugs" % "jsr305" % "3.0.2",
        "io.gsonfire" % "gson-fire" % "1.8.3" % "compile",
        "io.swagger.core.v3" % "swagger-annotations" % "2.0.0",
        "org.threeten" % "threetenbp" % "1.3.5" % "compile",
        "com.squareup.retrofit2" % "retrofit" % "2.6.2",
        "com.squareup.retrofit2" % "converter-scalars" % "2.6.2",
        "com.squareup.retrofit2" % "converter-gson" % "2.6.2",
        "org.apache.oltu.oauth2" % "org.apache.oltu.oauth2.client" % "1.0.2",
        "junit" % "junit" % "4.12" % "test",
      ),
      publishArtifact in (Compile, packageDoc) := false,
      publish / skip := true
    )

lazy val common = (project in file("common"))
    .settings(
      commonSettings ++ testSettings,
      name := "common",
      resolvers ++= allResolvers,
      libraryDependencies ++= Seq(
        sigmaState,
        ergoWallet
      ),
      publish / skip := true
    )

lazy val libApi = (project in file("lib-api"))
    .dependsOn(common % allConfigDependency)
    .settings(
      commonSettings ++ testSettings,
      resolvers ++= allResolvers,
      name := "lib-api",
      libraryDependencies ++= Seq(
      ),
      publish / skip := true
    )

lazy val libImpl = (project in file("lib-impl"))
    .dependsOn(javaClientGenerated % allConfigDependency, libApi % allConfigDependency)
    .settings(
      commonSettings ++ testSettings,
      name := "lib-impl",
      resolvers ++= allResolvers,
      libraryDependencies ++= Seq(
      ),
      publish / skip := true
    )

lazy val appkit = (project in file("appkit"))
    .dependsOn(
      common % allConfigDependency,
      javaClientGenerated % allConfigDependency,
      libApi % allConfigDependency,
      libImpl % allConfigDependency)
    .settings(commonSettings ++ testSettings,
      libraryDependencies ++= Seq(
        "com.squareup.okhttp3" % "mockwebserver" % "3.12.0",
        "org.graalvm.sdk" % "graal-sdk" % "19.2.0.1",
        "com.github.pureconfig" %% "pureconfig" % "0.12.1"
      ))
    .settings(publish / skip := true)

// examples depend on appkit
lazy val examples = (project in file("examples"))
    .dependsOn(
      common % allConfigDependency,
      libApi % allConfigDependency,
      javaClientGenerated % allConfigDependency,
      appkit % allConfigDependency,
      libImpl % allConfigDependency)
    .settings(
      commonSettings ++ testSettings,
      name := "examples",
      libraryDependencies ++= Seq(
        "com.squareup.okhttp3" % "mockwebserver" % "3.12.0",
        "org.graalvm.sdk" % "graal-sdk" % "19.2.0.1",
        "com.github.pureconfig" %% "pureconfig" % "0.12.1"
      ),
      publish / skip := true
    )


lazy val aggregateCompile = ScopeFilter(
  inProjects(common, javaClientGenerated, libApi, libImpl, appkit),
  inConfigurations(Compile))

lazy val rootSettings = Seq(
  sources in Compile := sources.all(aggregateCompile).value.flatten,
  libraryDependencies := libraryDependencies.all(aggregateCompile).value.flatten,
  mappings in (Compile, packageSrc) ++= (mappings in(Compile, packageSrc)).all(aggregateCompile).value.flatten,
  mappings in (Test, packageBin) ++= (mappings in(Test, packageBin)).all(aggregateCompile).value.flatten,
  mappings in(Test, packageSrc) ++= (mappings in(Test, packageSrc)).all(aggregateCompile).value.flatten,
)

lazy val root = (project in file("."))
    .aggregate(appkit, common, javaClientGenerated, libApi, libImpl)
    .settings(commonSettings ++ testSettings, rootSettings)
    .settings(publish / aggregate := false)
    .settings(publishLocal / aggregate := false)

