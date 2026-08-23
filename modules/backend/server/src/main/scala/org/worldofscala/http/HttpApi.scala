package org.worldofscala.http

import dev.cheleb.ziotapir.server.*
import org.worldofscala.auth.*
import org.worldofscala.earth.*
import org.worldofscala.health.HealthController
import org.worldofscala.organisation.*
import org.worldofscala.user.*
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.server.ServerEndpoint
import zio.*

import javax.sql.DataSource
import zio.telemetry.opentelemetry.tracing.Tracing
import com.augustnagro.magnum.ziomagnum.ZIOMagnumTracer
import com.augustnagro.magnum.SqlLogger

//https://tapir.softwaremill.com/en/latest/server/logic.html
type Deps = UserService & JWTService & OrganisationService & MeshService & Tracing

object HttpApi extends Routes[Deps] {

  type STREAMS = ZioStreams

  protected val makeControllers: ZIO[Deps, Nothing, List[BaseController[STREAMS]]] =
    for {
      _                      <- ZIO.debug("*******************\nGathering endpoints\n*****************")
      healthController       <- HealthController.makeZIO
      personController       <- UserController.makeZIO
      organisationController <- OrganisationController.makeZIO
      meshController         <- MeshController.makeZIO
    } yield List(healthController, personController, organisationController, meshController)

  /**
   * This is critical, to not provide the Postgres layer too early, it would be
   * closed too early in the app lifecycle.
   */
  def resolvedEndpoints
    : RIO[DataSource & ZIOMagnumTracer & SqlLogger & Tracing, List[ServerEndpoint[ZioStreams, Task]]] =
    endpoints.provideSome[DataSource & ZIOMagnumTracer & SqlLogger & Tracing](
      // Service layers
      UserServiceLive.layer,
      OrganisationServiceLive.layer,
      MeshServiceLive.layer,
      JWTServiceLive.configuredLayer,
      // Repository layers
      UserRepositoryLive.layer,
      OrganisationRepositoryLive.layer,
      MeshRepositoryLive.layer

      // , ZLayer.Debug.mermaid
    )
}
