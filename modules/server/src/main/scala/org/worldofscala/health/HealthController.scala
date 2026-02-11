package org.worldofscala.health

import dev.cheleb.ziotapir.BaseController
import org.worldofscala.http.endpoints.HealthEndpoint
import sttp.tapir.server.ServerEndpoint
import zio.*

class HealthController private extends BaseController with HealthEndpoint {

  val health = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("OK"))
  override val routes: List[ServerEndpoint[Any, Task]] =
    List(health)
}

object HealthController {
  def makeZIO = ZIO.succeed(new HealthController)
}
