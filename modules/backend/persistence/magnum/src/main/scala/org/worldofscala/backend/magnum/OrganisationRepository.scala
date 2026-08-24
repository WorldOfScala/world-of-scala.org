package org.worldofscala.backend.magnum

import com.augustnagro.magnum.*
import com.augustnagro.magnum.ziomagnum.*

//import com.augustnagro.magnum.ziomagnum.ZIOMagnumTracer

import io.scalaland.chimney.Transformer
import org.worldofscala.*
import org.worldofscala.domain.user.User
import org.worldofscala.domain.organisation.{Organisation, LatLon, Mesh}

import zio.*
import zio.stream.ZStream

import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

trait OrganisationRepository {
  def create(org: NewOrganisationEntity): Task[OrganisationEntity]
  def listAll(): Task[Seq[OrganisationEntity]]
  def streamAll(): UIO[ZStream[Any, Throwable, OrganisationEntity]]
}

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

object OrganisationEntity extends UUIDMapper[Organisation.Id](identity, Organisation.Id.apply):
  given Transformer[OrganisationEntity, Organisation] = Transformer.derive

class OrganisationRepositoryLive private (using DataSource, ZIOMagnumTracer, SqlLogger) extends OrganisationRepository {

  import OrganisationEntity.given

  val repo = Repo[NewOrganisationEntity, OrganisationEntity, Organisation.Id]

  override def streamAll(): UIO[ZStream[Any, Throwable, OrganisationEntity]] =
    sql"SELECT id, name, mesh_id, location, created_by, creation_date FROM organisations"
      .zStream[OrganisationEntity]("organisations")

  override def create(orga: NewOrganisationEntity): Task[OrganisationEntity] =
    repo.zInsertReturning(orga)

  override def listAll(): Task[Vector[OrganisationEntity]] =
    repo.zFindAll
}

object OrganisationRepositoryLive {

  def layer =
    ZLayer.derive[OrganisationRepositoryLive]
}
