package org.worldofscala.user

import dev.cheleb.scalamigen.NoPanel
import org.worldofscala.UUIDOpaque
import sttp.tapir.Schema
import zio.json.JsonCodec
import zio.prelude.*
import zio.prelude.Debug.Repr
import zio.prelude.magnolia.*

import java.time.OffsetDateTime
import java.util.UUID

@NoPanel
case class NewUser(
  firstname: String,
  lastname: String,
  email: String,
  password: Password,
  passwordConfirmation: Password
) derives JsonCodec,
      Schema,
      Debug {
  def errorMessages =
    NewUser.validate(this)
}

object NewUser {
  private val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".r

  def isValidEmail(email: String): Boolean =
    emailRegex.matches(email)

  def validate(newUser: NewUser): List[String] = {
    val emailErrors =
      if isValidEmail(newUser.email) then Nil else List("Invalid email format")
    val passwordErrors =
      if newUser.password == newUser.passwordConfirmation then Nil else List("Passwords do not match")
    emailErrors ++ passwordErrors
  }
}

opaque type Password <: String = String

object Password:
  given JsonCodec[Password] = JsonCodec.string
  given Schema[Password]    = Schema.string

  given Debug[Password] with
    def debug(value: Password): Repr = Repr.String("*****")

  def apply(password: String): Password = password

case class User(
  id: User.Id,
  firstname: String,
  lastname: String,
  email: String,
  hashedPassword: String,
  creationDate: OffsetDateTime
) derives JsonCodec,
      Schema

object User:

  opaque type Id <: UUID = UUID
  object Id extends UUIDOpaque[Id](JsonCodec.uuid, Schema.schemaForUUID):
    def apply(uuid: UUID): Id = uuid

case class UserID(id: User.Id, email: String) derives JsonCodec, Schema
