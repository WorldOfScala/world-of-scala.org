package org.worldofscala.user

import com.augustnagro.magnum.*
import com.augustnagro.magnum.ziomagnum.*
import io.scalaland.chimney.Transformer
import io.scalaland.chimney.dsl.*
import org.worldofscala.repository.UUIDMapper
import zio.*

import javax.sql.DataSource

trait UserRepository {
  def create(user: NewUserEntity): Task[UserEntity]
  def getById(id: User.Id): Task[Option[UserEntity]]
  def findByEmail(email: String): Task[Option[UserEntity]]
  def update(id: User.Id, op: UserEntity => UserEntity): Task[UserEntity]
  def delete(id: User.Id): Task[UserEntity]
}

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("users")
case class NewUserEntity(
  firstname: String,
  lastname: String,
  email: String,
  hashedPassword: String,
  creationDate: java.time.OffsetDateTime
) derives DbCodec

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("users")
case class UserEntity(
  @Id id: User.Id,
  firstname: String,
  lastname: String,
  email: String,
  hashedPassword: String,
  creationDate: java.time.OffsetDateTime
) derives DbCodec

object UserEntity extends UUIDMapper[User.Id](identity, User.Id.apply):
  given Transformer[UserEntity, User] = Transformer.derive

private class UserRepositoryLive private (using DataSource) extends UserRepository {

  import UserEntity.given

  val repo = Repo[NewUserEntity, UserEntity, User.Id]

  override def create(user: NewUserEntity): Task[UserEntity] =
    repo.zInsertReturning(user)

  override def getById(id: User.Id): Task[Option[UserEntity]] =
    repo.zFindById(id)

  override def findByEmail(email: String): Task[Option[UserEntity]] =
    val uspec = Spec[UserEntity]
      .where(sql"email = $email")
    repo.zFindAll(uspec).map(_.headOption)

  override def update(id: User.Id, op: UserEntity => UserEntity): Task[UserEntity] =
    for
      userEntity <- repo.zFindById(id).map(_.getOrElse(throw new RuntimeException(s"User $id not found")))
      updated     = op(userEntity)
      _          <-
        repo.zUpdate(updated)
    yield updated

  override def delete(id: User.Id): Task[UserEntity] =
    for
      userEntity <- repo.zFindById(id).map(_.getOrElse(throw new RuntimeException(s"User $id not found")))
      _          <- repo.zDeleteById(id)
    yield userEntity
}

object UserRepositoryLive {
  def layer: URLayer[DataSource, UserRepository] = ZLayer.derive[UserRepositoryLive]
}
