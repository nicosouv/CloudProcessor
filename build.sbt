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
      dependencyCheckDataDirectory := Some(
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

val dependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.2.15",
  "org.scalatest" %% "scalatest" % "3.2.15" % "test",
  "com.spotify" %% "scio-core" % "0.13.1",
  "com.spotify" %% "scio-jdbc" % "0.13.1",
  "com.spotify" %% "scio-extra" % "0.13.1",
  "com.spotify" %% "scio-repl" % "0.13.1",
  "com.spotify" %% "scio-test" % "0.13.1" % "test",
  "com.typesafe" % "config" % "1.4.2",
  "org.apache.beam" % "beam-runners-flink-1.16" % "2.47.0",
  "com.drewnoakes" % "metadata-extractor" % "2.18.0",
  "com.microsoft.azure" % "msal4j" % "1.13.10"
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