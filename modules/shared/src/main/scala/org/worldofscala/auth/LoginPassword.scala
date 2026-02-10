package org.worldofscala.auth

import dev.cheleb.scalamigen.NoPanel
import org.worldofscala.user.Password
import sttp.tapir.Schema

@NoPanel
final case class LoginPassword(login: String, password: Password) derives zio.json.JsonCodec, Schema:
  def isIncomplete: Boolean = login.isBlank || password.isBlank
