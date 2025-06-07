package org.worldofscala.http

import zio.*

import dev.cheleb.ziotapir.*

import org.worldofscala.auth.*
import org.worldofscala.earth.*
import org.worldofscala.health.HealthController
import org.worldofscala.organisation.*
import org.worldofscala.user.*

import org.worldofscala.repository.Repository
import sttp.tapir.server.ServerEndpoint
import sttp.capabilities.zio.ZioStreams
import io.getquill.jdbczio.Quill.Postgres
import io.getquill.SnakeCase

//https://tapir.softwaremill.com/en/latest/server/logic.html
type Deps = UserService & JWTService & OrganisationService & MeshService

object HttpApi extends Routes {

  private val makeControllers =
    for {
      _                      <- ZIO.debug("*******************\nGathering endpoints\n*****************")
      healthController       <- HealthController.makeZIO
      personController       <- UserController.makeZIO
      organisationController <- OrganisationController.makeZIO
      meshController         <- MeshController.makeZIO
    } yield List(healthController, personController, organisationController, meshController)

  private def endpointsZIO(ctrs: URIO[Deps, List[BaseController]]) =
    ctrs.map(gatherRoutes(_.routes))

  private def streamEndpointsZIO(ctrs: URIO[Deps, List[BaseController]]) =
    ctrs.map(gatherRoutes(_.streamRoutes))

  private def gatherRoutes: URIO[Deps, List[ServerEndpoint[ZioStreams, Task]]] =
    for {
      mem             <- makeControllers.memoize
      endpoints       <- endpointsZIO(mem)
      streamEndpoints <- streamEndpointsZIO(mem)
    } yield endpoints ++ streamEndpoints

  def endpoints =
    gatherRoutes.provideSome[Postgres[SnakeCase]](
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
