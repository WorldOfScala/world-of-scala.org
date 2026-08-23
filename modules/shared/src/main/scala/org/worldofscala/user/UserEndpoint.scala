package org.worldofscala.user

import org.worldofscala.BaseEndpoint
import org.worldofscala.auth.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import zio.*

object UserEndpoint extends BaseEndpoint:

  val create: PublicEndpoint[NewUser, Throwable, UserView, Any] = baseEndpoint
    .tag("person")
    .name("person")
    .post
    .in("person")
    .in(
      jsonBody[NewUser]
        .description("Person to create")
        .example(
          NewUser(
            "John",
            "Does",
            "john.doe@foo.bar",
            Password("notsecured"),
            Password("notsecured")
          )
        )
    )
    .out(jsonBody[UserView])
    .description("Create person")

  val login: PublicEndpoint[LoginPassword, Throwable, UserToken, Any] = baseEndpoint
    .tag("person")
    .name("login")
    .post
    .in("login")
    .in(
      jsonBody[LoginPassword]
    )
    .out(jsonBody[UserToken])
    .description("Login")

  val profile: Endpoint[String, Unit, Throwable, UserView, Any] = baseSecuredEndpoint
    .tag("person")
    .name("profile")
    .get
    .in("profile")
    .out(jsonBody[UserView])
    .description("Get profile")
