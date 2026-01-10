package org.worldofscala.http

import zio.*
import zio.http.*

import sttp.tapir.*
import sttp.tapir.files.*
import sttp.tapir.server.ziohttp.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.interceptor.cors.CORSInterceptor

import org.worldofscala.observability.*
import org.worldofscala.config.ServerConfig
import io.getquill.jdbczio.Quill.Postgres

import org.worldofscala.repository.Repository
import io.getquill.SnakeCase

object Server {

  private val staticEndpoints = staticResourcesGetServerEndpoint[Task](emptyInput)(
    this.getClass.getClassLoader,
    "public"
  ) :: Nil

  private def serverOptions: ZioHttpServerOptions[Any] =
    ZioHttpServerOptions.customiseInterceptors
      .metricsInterceptor(metricsInterceptor)
      .appendInterceptor(
        CORSInterceptor.default
      )
      .options

  private def build: ZIO[ServerConfig & Postgres[SnakeCase], Throwable, Unit] = for {
    serverConfig <- ZIO.service[ServerConfig]
    _            <- ZIO.logInfo(s"Starting server... http://localhost:${serverConfig.port}")
    apiEndpoints <- HttpApi.endpoints

    docEndpoints = SwaggerInterpreter()
                     .fromServerEndpoints(apiEndpoints, "World of scala", "1.0.0")
    serverLayer = zio.http.Server.defaultWith(config => config.binding("0.0.0.0", serverConfig.port))
    _          <- zio.http.Server
           .serve(
             ZioHttpInterpreter(serverOptions)
               .toHttp(metricsEndpoint :: apiEndpoints ::: docEndpoints ::: staticEndpoints)
           )
           .provideSomeLayer(serverLayer) <* Console.printLine("Server started !")
  } yield ()

  def start: Task[Unit] = build
    .provide(Repository.dataLayer, ServerConfig.layer)
}
