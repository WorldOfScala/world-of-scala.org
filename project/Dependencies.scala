import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val Versions = new {
    val chimney               = "1.8.2"
    val flywaydb              = "12.0.0"
    val frontroute            = "0.19.0"
    val iron                  = "3.2.1"
    val javaMail              = "1.6.2"
    val laminarFormDerivation = "1.4.0"
    val logback               = "1.5.27"
    val mUnit                 = "1.2.1"
    val osLib                 = "0.10.2"
    val postgresql            = "42.7.9"
    val scopt                 = "4.1.0"
    val slf4j                 = "2.0.17"
    val tapir                 = "1.13.6"
    val threesjs              = "0.0.8"
    val zio                   = "2.1.24"
    val zioConfig             = "4.0.5"
    val zioLaminarTapir       = "5.1.0"
    val zioLogging            = "2.5.1"
    val zioMagnum             = "2.0.0"
    val zioMock               = "1.0.0-RC12"
    val zioPrelude            = "1.0.0-RC44"
    val zioTestContainers     = "0.10.0"
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
    "dev.zio"       %% "zio-logging"       % Versions.zioLogging,
    "dev.zio"       %% "zio-logging-slf4j" % Versions.zioLogging,
    "ch.qos.logback" % "logback-classic"   % Versions.logback
  )

  private val magnumDependencies = Seq(
    "dev.cheleb"      %% "zio-magnum" % Versions.zioMagnum,
    "com.augustnagro" %% "magnumpg"   % "2.0.0-M2"
  )

  private val jwtDependencies = Seq(
    "com.auth0" % "java-jwt" % "4.5.0"
  )

  val serverLibraryDependencies =
    libraryDependencies ++= Seq(
      "io.scalaland"                %% "chimney"                  % Versions.chimney,
      "com.softwaremill.sttp.tapir" %% "tapir-zio"                % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"    % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % Versions.tapir,
      "dev.cheleb"                  %% "zio-tapir-server"         % Versions.zioLaminarTapir,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server"   % Versions.tapir   % Test,
      "dev.zio"                     %% "zio-test"                 % Versions.zio     % Test,
      "dev.zio"                     %% "zio-test-junit"           % Versions.zio     % Test,
      "dev.zio"                     %% "zio-test-sbt"             % Versions.zio     % Test,
      "dev.zio"                     %% "zio-test-magnolia"        % Versions.zio     % Test,
      "dev.zio"                     %% "zio-mock"                 % Versions.zioMock % Test
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
