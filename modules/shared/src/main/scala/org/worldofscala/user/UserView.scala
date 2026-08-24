package org.worldofscala.user

import dev.cheleb.scalamigen.NoPanel
import sttp.tapir.Schema
import zio.json.JsonCodec
import zio.prelude.*
import zio.prelude.Debug.Repr
import zio.prelude.magnolia.*

import java.time.OffsetDateTime

import sttp.tapir.generic.auto.*
import sttp.tapir.SchemaType.SString
import org.worldofscala.domain.user.User
import org.worldofscala.UUIDOpaque

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
  private val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z0-9]{2,}$".r

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

case class UserView(
  id: User.Id,
  firstname: String,
  lastname: String,
  email: String,
  hashedPassword: String,
  creationDate: OffsetDateTime
) derives JsonCodec,
      Schema

object UserView:

  given Debug[User.Id] with
    def debug(value: User.Id): Repr = Repr.String(value.toString)

  given JsonCodec[User.Id] = JsonCodec.uuid.transform(
    uuid => User.Id(uuid),
    id => id
  )
  given Schema[User.Id] =
    Schema(SString[User.Id]()).format("uuid")
  case class UserID(id: User.Id, email: String) derives JsonCodec, Schema
