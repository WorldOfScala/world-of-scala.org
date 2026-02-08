package org.worldofscala.organisation

import dev.cheleb.ziotapir.SecuredBaseController

import zio.*
import zio.json.*
import zio.stream.ZStream
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*

import org.worldofscala.auth.*
import org.worldofscala.user.UserID
import sttp.capabilities.zio.ZioStreams

class OrganisationController private (organisationService: OrganisationService, jwtService: JWTService)
    extends SecuredBaseController[String, UserID](jwtService.verifyToken) {

  val create: ServerEndpoint[Any, Task] = OrganisationEndpoint.create.zServerAuthenticatedLogic {
    userId => organisation =>
      organisationService.create(organisation, userId.id)
  }

  val listAll: ServerEndpoint[Any, Task] = OrganisationEndpoint.all.zServerLogic { _ =>
    organisationService.listAll()
  }

  val streamAll: ZServerEndpoint[Any, ZioStreams] = OrganisationEndpoint.allStream.zServerLogic { _ =>
    organisationService
      .streamAll()
      .map(_.flatMap(entity => ZStream.fromIterator((entity.toJson + "\n").getBytes().iterator)))

  }

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, listAll)

  override def streamRoutes: List[ServerEndpoint[ZioStreams, Task]] = List(streamAll)

}

object OrganisationController {
  def makeZIO: URIO[OrganisationService & JWTService, OrganisationController] =
    for
      organisationService <- ZIO.service[OrganisationService]
      jwtService          <- ZIO.service[JWTService]
    yield new OrganisationController(organisationService, jwtService)

}
