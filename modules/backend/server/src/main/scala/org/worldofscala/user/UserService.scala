package org.worldofscala.user

import io.scalaland.chimney.dsl.*
import org.worldofscala.auth.*
import org.worldofscala.domain.errors.InvalidCredentialsException
import org.worldofscala.domain.errors.UserAlreadyExistsException
import org.worldofscala.domain.errors.UserNotFoundException
import zio.*

import java.time.OffsetDateTime
import com.augustnagro.magnum.SqlException
import org.worldofscala.domain.user.{NewUser, User, UserID}
import org.worldofscala.domain.user.ports.UserPersistencePort

trait UserService {
  def register(irstname: String, lastname: String, email: String, password: String): Task[User]
  def login(email: String, password: String): Task[User]
  def getProfile(userId: UserID): Task[User]
}

class UserServiceLive private (
  userRepository: UserPersistencePort
) extends UserService {

  def register(firstname: String, lastname: String, email: String, password: String): Task[User] =
    for {
      _    <- ZIO.logDebug(s"Registering user: $email")
      user <- userRepository
                .create(
                  NewUser(
                    firstname = firstname,
                    lastname = lastname,
                    email = email,
                    hashedPassword = Hasher.generatedHash(password),
                    creationDate = OffsetDateTime.now()
                  )
                )
                .catchSome { case e: SqlException =>
                  ZIO.logError(s"Error while creating user: ${e.getMessage}")
                    *> ZIO.fail(UserAlreadyExistsException())

                }

    } yield user

  override def login(email: String, password: String): Task[User] =
    userRepository
      .findByEmail(email)
      .map {
        _.filter(user => Hasher.validateHash(password, user.hashedPassword))
      }
      .someOrFail(InvalidCredentialsException())

  override def getProfile(userId: UserID): Task[User] =
    for
      userEntity <- userRepository
                      .findByEmail(userId.email)
                      .someOrFail(UserNotFoundException(userId.email))
      user = userEntity.into[User].transform
    yield user

}

object UserServiceLive {
  val layer: RLayer[UserPersistencePort, UserService] =
    ZLayer.derive[UserServiceLive]
}
