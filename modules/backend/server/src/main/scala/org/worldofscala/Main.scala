package org.worldofscala

import org.worldofscala.http.Server
import org.worldofscala.services.FlywayService
import zio.*

import zio.logging.slf4j.bridge.Slf4jBridge
import zio.logging.LogFilter
import io.opentelemetry.api.OpenTelemetry
import kyo.*
import zio.telemetry.opentelemetry.bootstrap.ZIOpenTelemetryAppDefault
import sttp.tapir.server.interceptor.metrics.MetricsRequestInterceptor
import sttp.tapir.server.metrics.opentelemetry.OpenTelemetryMetrics
import io.opentelemetry.instrumentation.runtimetelemetry.RuntimeTelemetry

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

  val kyoWork: Unit < (Abort[Throwable] & Async) =
    Async
      .sleep(5.seconds)
      .andThen(42)
      .map(i => println(s"Kyo $i"))

  val program = for {

    given OpenTelemetry <- ZIO.service[OpenTelemetry]

    _ <- runtimeTelemetryEmitter()

    _ <- ZIO.logInfo("Starting World of Scala HTTP server...")

    _ <- ZIOs.run(kyoWork).fork

    _ <- FlywayService.runMigrations
    _ <- Server.start(otel4zMetricsInterceptor())
  } yield ()

  override def run =
    program

  private def runtimeTelemetryEmitter()(using otel: OpenTelemetry) =
    ZIO.attempt(RuntimeTelemetry.builder(otel).build()).withFinalizerAuto

  def otel4zMetricsInterceptor(
    instrumentationScopeName: String = "tapir"
  )(implicit otel: OpenTelemetry): MetricsRequestInterceptor[Task] = {
    val meter: io.opentelemetry.api.metrics.Meter = otel.meterBuilder(instrumentationScopeName).build()

    val metrics = OpenTelemetryMetrics.default[Task](meter)

    metrics.metricsInterceptor()
  }

end HttpServer
