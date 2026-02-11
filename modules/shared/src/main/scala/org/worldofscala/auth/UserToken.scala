package org.worldofscala.auth

import dev.cheleb.ziojwt.WithToken
import org.worldofscala.user.User
import zio.json.JsonCodec

final case class UserToken(id: User.Id, email: String, token: String, expiration: Option[Long]) extends WithToken
    derives JsonCodec
