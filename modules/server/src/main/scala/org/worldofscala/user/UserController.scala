package org.worldofscala.user

import dev.cheleb.ziotapir.SecuredBaseController
import org.worldofscala.auth.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*

class UserController private (personService: UserService, jwtService: JWTService)
    extends SecuredBaseController[String, UserID](jwtService.verifyToken) {

  private val create: ServerEndpoint[Any, Task] = UserEndpoint.create
    .zServerLogic:
      personService.register

  private val login: ServerEndpoint[Any, Task] = UserEndpoint.login.zServerLogic: lp =>
    for
      user  <- personService.login(lp.login, lp.password)
      token <- jwtService.createToken(user)
    yield token

  private val profile: ServerEndpoint[Any, Task] = UserEndpoint.profile.zServerAuthenticatedLogic: userId =>
    _ => personService.getProfile(userId)

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, login, profile)
}

object UserController {
  def makeZIO: URIO[UserService & JWTService, UserController] =
    for
      jwtService    <- ZIO.service[JWTService]
      personService <- ZIO.service[UserService]
    yield new UserController(personService, jwtService)
}
