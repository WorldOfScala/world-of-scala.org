package org.worldofscala.user

import zio.*

import io.scalaland.chimney.dsl.*

import org.worldofscala.auth.*

import java.sql.SQLException

import org.worldofscala.domain.errors.{InvalidCredentialsException, UserAlreadyExistsException, UserNotFoundException}

import dev.cheleb.ziochimney.*

import java.time.OffsetDateTime

trait UserService {
  def register(person: NewUser): Task[User]
  def login(email: String, password: String): Task[User]
  def getProfile(userId: UserID): Task[User]
}

class UserServiceLive private (
  userRepository: UserRepository
) extends UserService {

  def register(person: NewUser): Task[User] =
    for {
      _    <- ZIO.logDebug(s"Registering user: $person")
      user <- userRepository
                .create(
                  NewUserEntity(
                    firstname = person.firstname,
                    lastname = person.lastname,
                    email = person.email,
                    hashedPassword = Hasher.generatedHash(person.password.toString),
                    creationDate = OffsetDateTime.now()
                  )
                )
                .catchSome { case e: SQLException =>
                  ZIO.logError(s"Error code: ${e.getSQLState} while creating user: ${e.getMessage}")
                    *> ZIO.fail(UserAlreadyExistsException())
                }
                .mapInto[User]

    } yield user

  override def login(email: String, password: String): Task[User] =
    userRepository
      .findByEmail(email)
      .map {
        _.filter(user => Hasher.validateHash(password, user.hashedPassword))
      }
      .someOrFail(InvalidCredentialsException())
      .mapInto[User]

  override def getProfile(userId: UserID): Task[User] =
    for
      userEntity <- userRepository
                      .findByEmail(userId.email)
                      .someOrFail(UserNotFoundException(userId.email))
      user = userEntity.into[User].transform
    yield user

}

object UserServiceLive {
  val layer: RLayer[UserRepository, UserService] =
    ZLayer.derive[UserServiceLive]
}
