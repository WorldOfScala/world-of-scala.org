package org.worldofscala

import org.worldofscala.domain.errors.HttpError
import sttp.tapir.*

trait BaseEndpoint {
  val baseEndpoint: Endpoint[Unit, Unit, Throwable, Unit, Any] = endpoint
    .errorOut(statusCode and plainBody[String])
    .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
    .prependIn("api")

  val baseSecuredEndpoint: Endpoint[String, Unit, Throwable, Unit, Any] =
    baseEndpoint
      .tag("Admin")
      .securityIn(auth.bearer[String]())

}
