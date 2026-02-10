package org.worldofscala.earth

import org.worldofscala.auth.JWTService
import dev.cheleb.ziotapir.SecuredBaseController
import org.worldofscala.user.UserID

import zio.*

import sttp.tapir.server.*
import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import zio.stream.ZStream

class MeshController private (meshService: MeshService, jwtService: JWTService)
    extends SecuredBaseController[String, UserID](jwtService.verifyToken) {

  private val listAll: ServerEndpoint[Any, Task] = MeshEndpoint.all.zServerLogic: _ =>
    meshService.listAll()

  val createStream: ZServerEndpoint[Any, ZioStreams] = MeshEndpoint.streamCreate.zServerAuthenticatedLogic: _ =>
    (name, stream) => meshService.createStream(name, stream)

  private val putThumbnail: ZServerEndpoint[Any, ZioStreams] = MeshEndpoint.putThumbnail.zServerAuthenticatedLogic: _ =>
    (name, thumbnail) => meshService.updateThumnail(name, thumbnail)

  private val get: ZServerEndpoint[Any, ZioStreams] = MeshEndpoint.get.zServerLogic: id =>
    meshService.get(id).map(_.blob).map(ZStream.fromIterable)

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(listAll)

  override def streamRoutes: List[ServerEndpoint[ZioStreams, Task]] = List(createStream, putThumbnail, get)

}

object MeshController {
  def makeZIO: URIO[MeshService & JWTService, MeshController] =
    for
      meshService <- ZIO.service[MeshService]
      jwtService  <- ZIO.service[JWTService]
    yield new MeshController(meshService, jwtService)

}
