package org.worldofscala.user

import zio.*
import com.augustnagro.magnum.*
import com.augustnagro.magnum.ziomagnum.*
import io.scalaland.chimney.dsl.*

import org.worldofscala.repository.UUIDMapper
import javax.sql.DataSource
import io.scalaland.chimney.Transformer

trait UserRepository {
  def create(user: NewUserEntity): RIO[DataSource, UserEntity]
  def getById(id: User.Id): RIO[DataSource, Option[UserEntity]]
  def findByEmail(email: String): RIO[DataSource, Option[UserEntity]]
  def update(id: User.Id, op: UserEntity => UserEntity): RIO[DataSource, UserEntity]
  def delete(id: User.Id): RIO[DataSource, UserEntity]
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

  override def getById(id: User.Id): RIO[DataSource, Option[UserEntity]] =
    repo.zFindById(id)

  override def findByEmail(email: String): RIO[DataSource, Option[UserEntity]] =
    val uspec = Spec[UserEntity]
      .where(sql"email = $email")
    repo.zFindAll(uspec).map(_.headOption)

  override def update(id: User.Id, op: UserEntity => UserEntity): RIO[DataSource, UserEntity] =
    for
      userEntity <- repo.zFindById(id).map(_.getOrElse(throw new RuntimeException(s"User $id not found")))
      updated     = op(userEntity)
      _          <-
        repo.zUpdate(updated)
    yield updated

  override def delete(id: User.Id): RIO[DataSource, UserEntity] =
    for
      userEntity <- repo.zFindById(id).map(_.getOrElse(throw new RuntimeException(s"User $id not found")))
      _          <- repo.zDeleteById(id)
    yield userEntity
}

object UserRepositoryLive {
  def layer: URLayer[DataSource, UserRepository] = ZLayer.derive[UserRepositoryLive]
}
