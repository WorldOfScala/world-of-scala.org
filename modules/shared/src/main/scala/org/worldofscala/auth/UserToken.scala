package org.worldofscala.auth

import dev.cheleb.ziojwt.WithToken

import zio.json.*
import org.worldofscala.domain.user.User

final case class UserToken(
  id: User.Id,
  email: String,
  token: String,
  expiration: Option[Long]
) extends WithToken derives JsonCodec

object UserToken:
  import org.worldofscala.user.UserView.given
