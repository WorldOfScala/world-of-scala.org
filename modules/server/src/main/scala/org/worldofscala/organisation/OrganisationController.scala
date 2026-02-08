package org.worldofscala.organisation

import dev.cheleb.ziotapir.*

import zio.*
import zio.json.*

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*

import org.worldofscala.auth.*
import org.worldofscala.user.UserID
import sttp.capabilities.zio.ZioStreams
import zio.stream.ZStream
import zio.stream.ZPipeline

class OrganisationController private (organisationService: OrganisationService, jwtService: JWTService)
    extends SecuredBaseController[String, UserID](jwtService.verifyToken) {

  val create: ServerEndpoint[Any, Task] = OrganisationEndpoint.create.zServerAuthenticatedLogic:
    userId =>
      organisation =>
        organisationService
          .create(organisation, userId.id)

  val listAll: ServerEndpoint[Any, Task] = OrganisationEndpoint.all.zServerLogic:
    _ => organisationService.listAll()

  extension [R, A: JsonEncoder](stream: ZStream[R, Throwable, A])

    /**
     * Converts a ZStream of JSON-encodable values to a ZStream of bytes
     * representing JSON lines.
     *
     * @return
     *   A ZStream of bytes representing JSON lines.
     */
    def toJsonLinesStream: ZStream[R, Throwable, Byte] =
      stream >>> JsonEncoder[A].encodeJsonLinesPipeline >>> ZPipeline
        .map[Char, Byte](_.toByte)

  val streamAll: ZServerEndpoint[Any, ZioStreams] = OrganisationEndpoint.allStream.zServerLogic:
    _ =>
      ZIO.succeed:
        organisationService
          .streamAll()
          .toJsonLinesStream

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
