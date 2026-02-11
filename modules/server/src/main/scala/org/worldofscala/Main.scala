package org.worldofscala

import org.worldofscala.http.Server
import org.worldofscala.services.FlywayService
import zio.*
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object HttpServer extends ZIOAppDefault:

  override val bootstrap = SLF4J.slf4j(LogFormat.colored)

  override def run: Task[Unit] =
    for {
      _ <- ZIO.logInfo("Starting World of Scala HTTP server...")
      _ <- FlywayService.runMigrations
      _ <- Server.start
    } yield ()

end HttpServer
