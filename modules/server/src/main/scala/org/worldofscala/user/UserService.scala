package org.worldofscala.user

import dev.cheleb.ziochimney.*
import io.scalaland.chimney.dsl.*
import org.worldofscala.auth.*
import org.worldofscala.domain.errors.InvalidCredentialsException
import org.worldofscala.domain.errors.UserAlreadyExistsException
import org.worldofscala.domain.errors.UserNotFoundException
import zio.*

import java.time.OffsetDateTime
import com.augustnagro.magnum.SqlException

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
                .catchSome { case e: SqlException =>
                  ZIO.logError(s"Error while creating user: ${e.getMessage}")
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
