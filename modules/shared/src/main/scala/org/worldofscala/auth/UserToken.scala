package org.worldofscala.auth

import dev.cheleb.ziojwt.WithToken

import org.worldofscala.user.given
import zio.json.JsonCodec

final case class UserToken(
  id: org.worldofscala.domain.user.User.Id,
  email: String,
  token: String,
  expiration: Option[Long]
) extends WithToken derives JsonCodec
