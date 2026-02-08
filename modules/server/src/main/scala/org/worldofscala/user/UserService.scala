package org.worldofscala.user

import zio.*

import io.scalaland.chimney.dsl.*

import org.worldofscala.auth.*

import java.sql.SQLException

import org.worldofscala.domain.errors.{InvalidCredentialsException, UserNotFoundException, UserAlreadyExistsException}

import dev.cheleb.ziochimney.*
import org.worldofscala.repository.Repository
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
                .catchSome {
                  case e: SQLException =>
                    ZIO.logError(s"Error code: ${e.getSQLState} while creating user: ${e.getMessage}")
                      *> ZIO.fail(UserAlreadyExistsException())
                }
                .mapInto[User]
                .provideLayer(Repository.dataLayer)
    } yield user

  override def login(email: String, password: String): Task[User] =
    userRepository
      .findByEmail(email)
      .map {
        _.filter(
          user => Hasher.validateHash(password, user.hashedPassword)
        )
      }
      .someOrFail(InvalidCredentialsException())
      .mapInto[User]
      .provideLayer(Repository.dataLayer)

  override def getProfile(userId: UserID): Task[User] =
    for
      userEntity <- userRepository
                      .findByEmail(userId.email)
                      .provideLayer(Repository.dataLayer)
                      .someOrFail(UserNotFoundException(userId.email))
      user = userEntity.into[User].transform
    yield user

}

object UserServiceLive {
  val layer: RLayer[UserRepository, UserService] =
    ZLayer.derive[UserServiceLive]
}
