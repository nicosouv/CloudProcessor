ThisBuild / organization := "com.onedrive"

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := Compiler.scala213

// Format and style
ThisBuild / scalafmtOnCompile := true

// Scalafix
inThisBuild({
  if (sys.props.get("scalafix.disable").isEmpty) {
    List(
      semanticdbEnabled := true,
      scalafixDependencies ++= Seq(
        "com.github.liancheng" %% "organize-imports" % "0.6.0"
      ),
      semanticdbVersion := scalafixSemanticdb.revision,
      dependencyCheckFormat := "JUNIT",
      dependencyCheckDataDirectory := Option(
        (ThisBuild / baseDirectory).value / "data"
      )
    )
  } else List.empty
})

ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("staging") ++ Seq(
  "confluent" at "https://packages.confluent.io/maven/",
  "oss snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots/",
  "maven" at "https://maven.google.com"
)

val scioVersion = "0.13.6"
val beamVersion = "2.52.0"

val dependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.2.15",
  "org.scalatest" %% "scalatest" % "3.2.15" % "test",
  "com.spotify" %% "scio-core" % scioVersion,
  "com.spotify" %% "scio-jdbc" % scioVersion,
  "com.spotify" %% "scio-extra" % scioVersion,
  "com.spotify" %% "scio-repl" % scioVersion,
  "com.spotify" %% "scio-test" % scioVersion % "test",
  "com.typesafe" % "config" % "1.4.2",
  "org.apache.beam" % "beam-runners-flink-1.16" % beamVersion,
  "com.drewnoakes" % "metadata-extractor" % "2.18.0",
  "com.microsoft.azure" % "msal4j" % "1.13.10",
  "com.azure" % "azure-identity" % "1.9.1",
  "com.microsoft.graph" % "microsoft-graph" % "5.58.0",
  "com.dropbox.core" % "dropbox-core-sdk" % "5.4.5",
  "com.google.api-client" % "google-api-client" % "2.2.0",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.34.1",
  "com.google.apis" % "google-api-services-drive" % "v3-rev20220815-2.0.0"
)

lazy val root = (project in file("."))
  .settings(
    name := "OneDriveImageProcessor",
    scalaVersion := Compiler.scala213,
    libraryDependencies ++= dependencies,
    packMain := Map(
      "onedrive image processor" -> "com.onedrive.image.processor.OrganizerPipeline",
      "pipeline example" -> "example.PipelineExample"
    ),
    packJvmOpts := Map(),
    packGenerateWindowsBatFile := false,
    packJarNameConvention := "default",
    packJarListFile := Some("lib/jars.mf"),
    packExpandedClasspath := false,
    packResourceDir += (baseDirectory.value / "web" -> "web-content")
  )
  .enablePlugins(PackPlugin)
