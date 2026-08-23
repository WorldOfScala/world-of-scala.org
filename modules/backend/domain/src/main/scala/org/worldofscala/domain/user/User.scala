package org.worldofscala.domain.user

import java.time.OffsetDateTime
import java.util.UUID

case class NewUser(
  firstname: String,
  lastname: String,
  email: String,
  hashedPassword: String,
  creationDate: java.time.OffsetDateTime
)

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
  object Id:
    def apply(uuid: UUID): Id = uuid

case class UserID(id: User.Id, email: String)
