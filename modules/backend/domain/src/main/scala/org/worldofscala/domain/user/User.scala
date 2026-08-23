package org.worldofscala.domain.user

import java.time.OffsetDateTime
import java.util.UUID

case class NewUser(
  firstname: String,
  lastname: String,
  email: String,
  password: Password,
  passwordConfirmation: Password
):
  def errorMessages =
    NewUser.validate(this)

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

  def apply(password: String): Password = password

case class User(
  id: User.Id,
  firstname: String,
  lastname: String,
  email: String,
  hashedPassword: String,
  creationDate: OffsetDateTime
)
object User:

  opaque type Id <: UUID = UUID

case class UserID(id: User.Id, email: String)
