package org.worldofscala.http

import com.augustnagro.magnum.ziomagnum.Slf4jMagnumLogger

import org.worldofscala.config.ServerConfig
import org.worldofscala.observability.*
import org.worldofscala.repository.*
import sttp.tapir.*
import sttp.tapir.files.*
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.ziohttp.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio.*
import zio.http.*

import javax.sql.DataSource
import io.opentelemetry.api.OpenTelemetry
import zio.telemetry.opentelemetry.tracing.Tracing
import sttp.tapir.server.ziopentelemetry.ZIOpenTelemetryTracing
import com.augustnagro.magnum.ziomagnum.ZIOpenTelemetryMagnumTracer
import sttp.tapir.server.interceptor.metrics.MetricsRequestInterceptor

/**
 * This is the main entry point for the HTTP server.
 *
 * It gathers all the endpoints from the controllers and starts the server.
 */
object Server {

  /**
   * This is a simple endpoint to serve static resources from the "public"
   * folder in the classpath.
   */
  private val staticEndpoints = staticResourcesGetServerEndpoint[Task](emptyInput)(
    this.getClass.getClassLoader,
    "public"
  ) :: Nil

  private def serverOptions(metricsInterceptor: MetricsRequestInterceptor[Task])(using
    tracing: Tracing
  ): ZioHttpServerOptions[Any] = ZioHttpServerOptions.customiseInterceptors
    .prependInterceptor(
      ZIOpenTelemetryTracing(tracing)
    )
    .appendInterceptor(
      CORSInterceptor.default
    )
    .appendInterceptor(metricsInterceptor)
    .serverLog(
      ZioHttpServerOptions.defaultServerLog[Any]
    )
    .options

  private def build(metricsInterceptor: MetricsRequestInterceptor[Task]) = for {
    serverConfig <- ZIO.service[ServerConfig]

    given Tracing <- ZIO.service[Tracing]
    _             <- ZIO.logInfo(s"Starting server... http://localhost:${serverConfig.port}")

    apiEndpoints <- HttpApi.resolvedEndpoints

    docEndpoints = SwaggerInterpreter()
                     .fromServerEndpoints(apiEndpoints, "World of scala", "1.0.0")
    serverLayer = zio.http.Server.defaultWith(config => config.binding("0.0.0.0", serverConfig.port))
    _          <- zio.http.Server
           .serve(
             ZioHttpInterpreter(serverOptions(metricsInterceptor))
               .toHttp(metricsEndpoint :: apiEndpoints ::: docEndpoints ::: staticEndpoints)
           )
           .provideSomeLayer(serverLayer) <* Console.printLine("Server started !")
  } yield ()

  def start(metricsInterceptor: MetricsRequestInterceptor[Task]): RIO[OpenTelemetry & Tracing, Unit] =
    build(metricsInterceptor)
      .provideSome[Tracing](
        Slf4jMagnumLogger.live(),
        ZIOpenTelemetryMagnumTracer.live,
        ServerConfig.layer,
        datasourceLayer
      )

}
