package org.worldofscala.backend.magnum

import com.augustnagro.magnum.*
import com.augustnagro.magnum.ziomagnum.*

//import com.augustnagro.magnum.ziomagnum.ZIOMagnumTracer

import io.scalaland.chimney.Transformer
import dev.cheleb.ziochimney.*
import org.worldofscala.*
import org.worldofscala.domain.user.User
import org.worldofscala.domain.organisation.{Organisation, LatLon, Mesh, OrganisationPersistencePort}

import zio.*
import zio.stream.ZStream

import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

import org.worldofscala.backend.magnum.UserEntity.given
import MeshEntity.given

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("organisations")
case class NewOrganisationEntity(
  name: String,
  meshId: Option[Mesh.Id],
  location: LatLon,
  createdBy: User.Id,
  creationDate: java.time.OffsetDateTime
) derives DbCodec

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("organisations")
case class OrganisationEntity(
  @Id id: Organisation.Id,
  name: String,
  meshId: Option[Mesh.Id],
  location: LatLon,
  createdBy: User.Id,
  creationDate: java.time.OffsetDateTime
) derives DbCodec

object OrganisationEntity extends UUIDMapper[Organisation.Id](identity, Organisation.Id.apply)

class OrganisationMagnumPersistenceAdapterLive private (using DataSource, ZIOMagnumTracer, SqlLogger)
    extends OrganisationPersistencePort {

  import OrganisationEntity.given

  val repo = Repo[NewOrganisationEntity, OrganisationEntity, Organisation.Id]

  override def streamAll(): UIO[ZStream[Any, Throwable, Organisation]] =
    sql"SELECT id, name, mesh_id, location, created_by, creation_date FROM organisations"
      .zStream[OrganisationEntity]("organisations")
      .map(_.mapInto[Organisation])

  override def create(
    name: String,
    meshId: Option[Mesh.Id],
    location: LatLon,
    createdBy: User.Id,
    creationDate: java.time.OffsetDateTime
  ): Task[Organisation] =
    repo
      .zInsertReturning(new NewOrganisationEntity(name, meshId, location, createdBy, creationDate))
      .mapInto[Organisation]

  override def listAll(): Task[Vector[Organisation]] =
    repo.zFindAll
      .mapInto[Organisation]
}

object OrganisationMagnumPersistenceAdapterLive {

  def layer =
    ZLayer.derive[OrganisationMagnumPersistenceAdapterLive]
}
