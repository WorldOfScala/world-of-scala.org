import java.nio.charset.StandardCharsets
import org.scalajs.linker.interface.ModuleSplitStyle

import Dependencies._
//
// Will handle different build modes:
// - prod: production mode, aka with BFF and webjar deployment
// - demo: demo mode (default)
// - dev:  development mode
//
import DeploymentSettings._

val scala3 = "3.7.4"

name := "World Of Scala"

inThisBuild(
  List(
    scalaVersion                            := scala3,
    dependencyOverrides += "org.scala-lang" %% "scala3-library" % scala3, // ScalaJS workaround
    semanticdbEnabled                       := true,
    semanticdbVersion                       := scalafixSemanticdb.revision,
    fullstackJsProject                      := client,
    fullstackJvmProject                     := Some(server),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
//      "-Xfatal-warnings"
    ),
    run / fork := true,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
)

//
// This is static generation settings to be used in server project
// Illustrate how to use the generator project to generate static files with twirl
//
// Aggregate root project
// This is the root project that aggregates all other projects
// It is used to run tasks on all projects at once.
lazy val root = project
  .in(file("."))
  .aggregate(
    server,
    sharedJs,
    sharedJvm,
    client
  )
  .disablePlugins(RevolverPlugin)
  .settings(
    publish / skip := true
  )

//
// Server project
// It depends on sharedJvm project, a project that contains shared code between server and client
//
lazy val server = project
  .in(file("modules/server"))
  .enablePlugins(FullstackPlugin, SbtTwirl, JavaAppPackaging, DockerPlugin, AshScriptPlugin)
  .settings(
    staticGenerationSettings(client)
  )
  .settings(
    fork := true,
    serverLibraryDependencies,
    testingLibraryDependencies
  )
  .settings(dockerSettings: _*)
  .dependsOn(sharedJvm)
  .settings(
    publish / skip := true
  )

val usedScalacOptions = Seq(
  "-encoding",
  "utf8",
  "-unchecked",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xmax-inlines:64",
  "-Wunused:all"
)

//
// Client project
// It depends on sharedJs project, a project that contains shared code between server and client.
//
lazy val client = scalajsProject("client")
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { config =>
      mode match {
        case "ESModule" =>
          config
            .withModuleKind(ModuleKind.ESModule)
        case _ =>
          config
            .withModuleKind(ModuleKind.ESModule)
            .withSourceMap(false)
            .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("org.worldofscala.app")))
      }
    }
  )
  .settings(scalacOptions ++= usedScalacOptions)
  .settings(clientLibraryDependencies)
  .dependsOn(sharedJs)
  .settings(
    publish / skip := true
  )

//
// Shared project
// It is a cross project that contains shared code between server and client
//
lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys    := Seq[BuildInfoKey](version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.worldofscala"
  )
  // .disablePlugins(RevolverPlugin)
  .in(file("modules/shared"))
  .settings(
    sharedJvmAndJsLibraryDependencies,
    testingLibraryDependencies
  )
  .settings(
    publish / skip := true
  )
lazy val sharedJvm = shared.jvm

lazy val sharedJs = shared.js

Test / fork := false

def scalajsProject(projectId: String): Project =
  Project(
    id = projectId,
    base = file(s"modules/$projectId")
  )
    .enablePlugins(ScalaJSPlugin)
    .disablePlugins(RevolverPlugin)
    .settings(nexusNpmSettings)
    .settings(Test / requireJsDomEnv := true)
    .settings(
      scalacOptions := Seq(
        "-scalajs",
        "-deprecation",
        "-feature"
//        "-Xfatal-warnings"
      )
    )
