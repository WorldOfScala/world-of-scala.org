import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val Versions = new {
    val chimney                       = "1.10.0"
    val flywaydb                      = "12.9.0"
    val frontroute                    = "0.19.0"
    val iron                          = "3.3.1"
    val kyo                           = "1.0.0-RC4"
    val javaMail                      = "1.6.2"
    val laminarFormDerivation         = "1.8.2"
    val logback                       = "1.5.34"
    val mUnit                         = "1.3.3"
    val osLib                         = "0.11.8"
    val postgresql                    = "42.7.11"
    val scopt                         = "4.1.0"
    val slf4j                         = "2.0.18"
    val tapir                         = "1.13.24"
    val threesjs                      = "0.1.0"
    val zio                           = "2.1.26"
    val zioConfig                     = "4.0.7"
    val zioLogging                    = "2.5.3"
    val zioLaminarTapir               = "8.1.0"
    val zioOpenTelemetryBootstrap     = "0.0.1"
    val openTelemetryRuntimeTelemetry = "2.29.0-alpha"
    val zioMagnum                     = "3.0.0"
    val zioMock                       = "1.0.0-RC12"
    val zioPrelude                    = "1.0.0-RC47"
    val zioSchema                     = "1.8.5"
    val zioTestContainers             = "0.10.0"
  }

  private val configDependencies = Seq(
    "dev.zio" %% "zio-config"          % Versions.zioConfig,
    "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig,
    "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig
  )

  private val databaseDependencies = Seq(
    "org.flywaydb"           % "flyway-core"                       % Versions.flywaydb,
    "org.flywaydb"           % "flyway-database-postgresql"        % Versions.flywaydb,
    "org.postgresql"         % "postgresql"                        % Versions.postgresql,
    "io.github.scottweaver" %% "zio-2-0-testcontainers-postgresql" % Versions.zioTestContainers % Test
  )

  private val loggingDependencies = Seq(
    "dev.zio" %% "zio-logging"               % Versions.zioLogging,
    "dev.zio" %% "zio-logging-slf4j"         % Versions.zioLogging,
    "dev.zio" %% "zio-logging-slf4j2-bridge" % Versions.zioLogging
  )

  private val magnumDependencies = Seq(
    "dev.cheleb" %% "zio-magnum-opentelemetry" % Versions.zioMagnum
  )

  private val jwtDependencies = Seq(
    "com.auth0" % "java-jwt" % "4.5.2"
  )

  val serverLibraryDependencies =
    libraryDependencies ++= Seq(
      "io.getkyo"                       %% "kyo-prelude"                     % Versions.kyo,
      "io.getkyo"                       %% "kyo-scheduler-zio"               % Versions.kyo,
      "io.getkyo"                       %% "kyo-zio"                         % Versions.kyo,
      "io.scalaland"                    %% "chimney"                         % Versions.chimney,
      "com.softwaremill.sttp.tapir"     %% "tapir-zio"                       % Versions.tapir,
      "com.softwaremill.sttp.tapir"     %% "tapir-zio-opentelemetry"         % Versions.tapir,
      "com.softwaremill.sttp.tapir"     %% "tapir-opentelemetry-metrics"     % Versions.tapir,
      "com.softwaremill.sttp.tapir"     %% "tapir-zio-http-server"           % Versions.tapir,
      "com.softwaremill.sttp.tapir"     %% "tapir-prometheus-metrics"        % Versions.tapir,
      "com.softwaremill.sttp.tapir"     %% "tapir-swagger-ui-bundle"         % Versions.tapir,
      "dev.cheleb"                      %% "zio-tapir-server"                % Versions.zioLaminarTapir,
      "dev.cheleb"                      %% "zio-opentelemetry-bootstrap"     % Versions.zioOpenTelemetryBootstrap,
      "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry" % Versions.openTelemetryRuntimeTelemetry,
      "com.softwaremill.sttp.tapir"     %% "tapir-sttp-stub-server"          % Versions.tapir   % Test,
      "dev.zio"                         %% "zio-test"                        % Versions.zio     % Test,
      "dev.zio"                         %% "zio-test-junit"                  % Versions.zio     % Test,
      "dev.zio"                         %% "zio-test-sbt"                    % Versions.zio     % Test,
      "dev.zio"                         %% "zio-test-magnolia"               % Versions.zio     % Test,
      "dev.zio"                         %% "zio-mock"                        % Versions.zioMock % Test
    ) ++
      configDependencies ++
      databaseDependencies ++
      magnumDependencies ++
      jwtDependencies ++
      loggingDependencies

  val testingLibraryDependencies =
    libraryDependencies ++= Seq(
      "org.scalameta" %%% "munit"        % Versions.mUnit % Test,
      "dev.zio"       %%% "zio-test"     % Versions.zio   % Test,
      "dev.zio"       %%% "zio-test-sbt" % Versions.zio   % Test
    )

  val sharedJvmAndJsLibraryDependencies: Setting[Seq[ModuleID]] =
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %%% "tapir-zio"                      % Versions.tapir,
      "com.softwaremill.sttp.tapir" %%% "tapir-iron"                     % Versions.tapir,
      "com.softwaremill.sttp.tapir" %%% "tapir-json-zio"                 % Versions.tapir,
      "dev.cheleb"                  %%% "laminar-form-derivation-shared" % Versions.laminarFormDerivation,
      "dev.cheleb"                  %%% "zio-tapir-shared"               % Versions.zioLaminarTapir,
      "dev.zio"                     %%% "zio-prelude"                    % Versions.zioPrelude,
      "dev.zio"                     %%% "zio-prelude-magnolia"           % Versions.zioPrelude,
      "dev.zio"                     %%% "zio-schema-json"                % Versions.zioSchema,
      "io.github.iltotore"           %% "iron-zio-json"                  % Versions.iron
    )

  val clientLibraryDependencies: Setting[Seq[ModuleID]] =
    libraryDependencies ++= Seq(
      // pull laminar 17.1.0
      "dev.cheleb" %%% "laminar-form-derivation-ui5" % Versions.laminarFormDerivation,
      // pull tapir-sttp-client and zio-tapir
      "dev.cheleb"    %%% "zio-tapir-laminar" % Versions.zioLaminarTapir,
      "io.frontroute" %%% "frontroute"        % Versions.frontroute,
      "dev.cheleb"    %%% "threesjs"          % Versions.threesjs
    )

  val clientAndServerLibraries = Seq(
  )

  val staticFilesGeneratorDependencies =
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt"        % Versions.scopt,
      "com.lihaoyi"      %% "os-lib"       % Versions.osLib,
      "org.slf4j"         % "slf4j-simple" % Versions.slf4j
    )
}
