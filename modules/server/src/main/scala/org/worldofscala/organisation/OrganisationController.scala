package org.worldofscala.organisation

import dev.cheleb.ziotapir.server.*
import org.worldofscala.auth.*
import org.worldofscala.user.UserID
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*
import zio.json.*

import zio.telemetry.opentelemetry.tracing.Tracing

class OrganisationController private (
  organisationService: OrganisationService,
  jwtService: JWTService,
  tracing: Tracing
) extends SecuredBaseController[String, UserID, ZioStreams](jwtService.verifyToken) {

  import tracing.aspects.*

  val create: ServerEndpoint[Any, Task] = OrganisationEndpoint.create.zServerAuthenticatedLogic: userId =>
    organisation =>
      organisationService
        .create(organisation, userId.id)

  val listAll: ServerEndpoint[Any, Task] = OrganisationEndpoint.all.zServerLogic: _ =>
    organisationService.listAll() @@ span("listAll-organisations")

  val streamAll: ZServerEndpoint[Any, ZioStreams] = OrganisationEndpoint.allStream.zServerLogic: _ =>
    for stream <- organisationService
                    .streamAll()
                    .toJsonLinesStream
    yield stream

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, listAll)

  override def streamRoutes: List[ServerEndpoint[ZioStreams, Task]] = List(streamAll)

}

object OrganisationController {
  def makeZIO: URIO[OrganisationService & JWTService & Tracing, OrganisationController] =
    for
      organisationService <- ZIO.service[OrganisationService]
      jwtService          <- ZIO.service[JWTService]
      tracing             <- ZIO.service[Tracing]
    yield new OrganisationController(organisationService, jwtService, tracing)

}
