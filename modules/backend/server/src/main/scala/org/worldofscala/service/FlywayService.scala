package org.worldofscala.services

import org.flywaydb.core.Flyway
import org.worldofscala.config.Configs
import org.worldofscala.config.FlywayConfig
import zio.*

trait FlywayService {
  def runClean(): Task[Unit]
  def runBaseline(): Task[Unit]
  def runMigrations(): Task[Unit]
  def runRepair(): Task[Unit]
}

object FlywayService:
  def runMigrations: Task[Unit] =
    build
      .provide(FlywayServiceLive.configuredLayer)

  private def build: RIO[FlywayService, Unit] =
    for {
      flyway <- ZIO.service[FlywayService]
      _      <- flyway.runMigrations().catchSome { case e =>
             ZIO.logError(s"Error running migrations: ${e.getMessage()}")
               *> flyway.runRepair() *> flyway.runMigrations()
           }
    } yield ()

class FlywayServiceLive private (flyway: Flyway) extends FlywayService {
  override def runClean(): Task[Unit]      = ZIO.attemptBlocking(flyway.clean())
  override def runBaseline(): Task[Unit]   = ZIO.attemptBlocking(flyway.baseline())
  override def runMigrations(): Task[Unit] = ZIO.attemptBlocking(flyway.migrate())
  override def runRepair(): Task[Unit]     = ZIO.attemptBlocking(flyway.repair())
}

object FlywayServiceLive {
  def live: RLayer[FlywayConfig, FlywayService] = ZLayer(
    for {
      config <- ZIO.service[FlywayConfig]
      flyway <- ZIO.attempt(
                  Flyway
                    .configure()
                    .loggers("slf4j")
                    .dataSource(config.url, config.user, config.password)
                    .load()
                )
    } yield new FlywayServiceLive(flyway)
  )

  val configuredLayer: TaskLayer[FlywayService] = Configs.makeConfigLayer[FlywayConfig]("db.dataSource") >>> live
}
