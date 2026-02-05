package org.worldofscala.user

import zio.*
import com.augustnagro.magnum.*
import io.scalaland.chimney.dsl.*

import org.worldofscala.repository.UUIDMapper

trait UserRepository {
  def create(user: NewUserEntity): Task[UserEntity]
  def getById(id: User.Id): Task[Option[UserEntity]]
  def findByEmail(email: String): Task[Option[UserEntity]]
  def update(id: User.Id, op: UserEntity => UserEntity): Task[UserEntity]
  def delete(id: User.Id): Task[UserEntity]
}

object UserRepository extends UUIDMapper[User.Id](identity, User.Id.apply)

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
case class NewUserEntityRepo(
  id: Option[User.Id],
  firstname: String,
  lastname: String,
  email: String,
  hashedPassword: String,
  creationDate: java.time.ZonedDateTime
) derives DbCodec

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
case class UserEntityRepo(
  @Id id: User.Id,
  firstname: String,
  lastname: String,
  email: String,
  hashedPassword: String,
  creationDate: java.time.ZonedDateTime
) derives DbCodec

class UserRepositoryLive private (transactor: Transactor) extends UserRepository {

  import UserRepository.given

  override def create(user: NewUserEntity): Task[UserEntity] =
    transact(transactor) {
      val repo =
        NewUserEntityRepo(user.id, user.firstname, user.lastname, user.email, user.hashedPassword, user.creationDate)
      val created = repo.create
      UserEntity(
        created.id.get,
        created.firstname,
        created.lastname,
        created.email,
        created.hashedPassword,
        created.creationDate
      )
    }

  override def getById(id: User.Id): Task[Option[UserEntity]] =
    transact(transactor) {
      sql"SELECT id, firstname, lastname, email, hashed_password, creation_date FROM users WHERE id = $id"
        .query[UserEntityRepo]
        .run()
        .headOption
        .map(_.map(r => UserEntity(r.id, r.firstname, r.lastname, r.email, r.hashedPassword, r.creationDate)))
    }

  override def findByEmail(email: String): Task[Option[UserEntity]] =
    transact(transactor) {
      sql"SELECT id, firstname, lastname, email, hashed_password, creation_date FROM users WHERE email = $email"
        .query[UserEntityRepo]
        .run()
        .headOption
        .map(_.map(r => UserEntity(r.id, r.firstname, r.lastname, r.email, r.hashedPassword, r.creationDate)))
    }

  override def update(id: User.Id, op: UserEntity => UserEntity): Task[UserEntity] =
    for {
      userEntity <- getById(id).someOrFail(new RuntimeException(s"User $id not found"))
      updated     = op(userEntity)
      result     <- transact(transactor) {
                  val repo = UserEntityRepo(
                    updated.id,
                    updated.firstname,
                    updated.lastname,
                    updated.email,
                    updated.hashedPassword,
                    updated.creationDate
                  )
                  repo.update
                  updated
                }
    } yield result

  override def delete(id: User.Id): Task[UserEntity] =
    transact(transactor) {
      sql"DELETE FROM users WHERE id = $id RETURNING id, firstname, lastname, email, hashed_password, creation_date"
        .query[UserEntityRepo]
        .run()
        .headOption
        .map(_.map(r => UserEntity(r.id, r.firstname, r.lastname, r.email, r.hashedPassword, r.creationDate)))
        .getOrElse(throw new RuntimeException(s"User $id not found"))
    }
}

object UserRepositoryLive {
  def layer: RLayer[Transactor, UserRepository] = ZLayer.derive[UserRepositoryLive]
}
