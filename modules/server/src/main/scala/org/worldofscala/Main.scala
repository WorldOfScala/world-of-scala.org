package org.worldofscala

import org.worldofscala.http.Server
import org.worldofscala.services.FlywayService
import zio.*
import sttp.tapir.server.ziopentelemetry.ZIOpenTelemetryAppDefault

import zio.logging.slf4j.bridge.Slf4jBridge
import zio.logging.LogFilter
import io.opentelemetry.api.OpenTelemetry

object HttpServer extends ZIOpenTelemetryAppDefault("World of Scala"):

  val logFilterConfig = LogFilter.LogLevelByNameConfig(
    LogLevel.Info,
    "com.augustnagro.magnum" -> LogLevel.Debug,
    "zio.logging.slf4j"      -> LogLevel.Debug,
    "SLF4J-LOGGER"           -> LogLevel.Warning
  )

  override def baseLogLayer: ZLayer[Any, Nothing, Unit] = Slf4jBridge.init(
    logFilterConfig.toFilter
  )

  val program = for {

    given OpenTelemetry <- ZIO.service[OpenTelemetry]

    _ <- ZIO.logInfo("Starting World of Scala HTTP server...")
    _ <- FlywayService.runMigrations
    _ <- Server.start(otel4zMetricsInterceptor())
  } yield ()

  override def run =
    program

end HttpServer
