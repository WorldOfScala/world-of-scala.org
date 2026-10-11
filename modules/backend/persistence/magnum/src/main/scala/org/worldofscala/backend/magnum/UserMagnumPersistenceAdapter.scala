package org.worldofscala.backend.magnum

import com.augustnagro.magnum.*
import com.augustnagro.magnum.ziomagnum.*
import io.scalaland.chimney.Transformer
import io.scalaland.chimney.dsl.*
import zio.*

import javax.sql.DataSource

import dev.cheleb.ziochimney.*

import org.worldofscala.domain.user.*
import org.worldofscala.domain.user.ports.UserPersistencePort

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

private class UserMagnumPersistenceAdapterLive private (using DataSource, SqlLogger) extends UserPersistencePort {

  import UserEntity.given

  val repo = Repo[NewUserEntity, UserEntity, User.Id]

  override def create(user: NewUser): Task[User] =
    repo.zInsertReturning(user.transformInto[NewUserEntity]).mapInto[User]

  override def getById(id: User.Id): Task[Option[User]] =
    repo
      .zFindById(id)
      .mapInto[User]

  override def findByEmail(email: String): Task[Option[User]] =
    val uspec = Spec[UserEntity]
      .where(sql"email = $email")
    repo
      .zFindAll(uspec)
      .map(_.headOption)
      .mapInto[User]

  override def update(id: User.Id, op: User => User): Task[User] =
    for
      userEntity <- repo.zFindById(id).map(_.getOrElse(throw new RuntimeException(s"User $id not found")))
      updated     = op(userEntity.transformInto[User]).transformInto[UserEntity]
      _          <-
        repo.zUpdate(updated)
    yield updated.transformInto[User]

  override def delete(id: User.Id): Task[User] =
    for
      userEntity <- repo.zFindById(id).map(_.getOrElse(throw new RuntimeException(s"User $id not found")))
      _          <- repo.zDeleteById(id)
    yield userEntity.transformInto[User]
}

object UserMagnumPersistenceAdapterLive {
  def layer: URLayer[DataSource & ZIOMagnumTracer & SqlLogger, UserPersistencePort] =
    ZLayer.derive[UserMagnumPersistenceAdapterLive]
}
