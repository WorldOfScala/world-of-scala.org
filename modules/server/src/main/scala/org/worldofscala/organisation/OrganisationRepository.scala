package org.worldofscala.organisation

import com.augustnagro.magnum.*
import com.augustnagro.magnum.ziomagnum.*
import org.worldofscala.*
import org.worldofscala.user.User
import org.worldofscala.earth.Mesh

import zio.*
import zio.stream.ZStream

import org.worldofscala.repository.UUIDMapper
import javax.sql.DataSource

import org.worldofscala.user.UserEntity
import org.worldofscala.earth.MeshEntity
import org.postgresql.geometric.PGpoint
import java.sql.ResultSet
import java.sql.PreparedStatement
import io.scalaland.chimney.Transformer

trait OrganisationRepository {
  def create(org: NewOrganisationEntity): Task[OrganisationEntity]
  def listAll(): Task[List[OrganisationEntity]]
  def streamAll(): ZStream[Any, Throwable, OrganisationEntity]
}

import UserEntity.given
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

class OrganisationRepositoryLive private (using DataSource) extends OrganisationRepository {

  import OrganisationEntity.given

  val repo = Repo[NewOrganisationEntity, OrganisationEntity, Organisation.Id]

  override def streamAll(): ZStream[Any, Throwable, OrganisationEntity] =
    sql"SELECT id, name, mesh_id, location, created_by, creation_dates FROM organisations".zStream[OrganisationEntity]()
    // ZStream.fromIterableZIO {
    //   listAll()
    // }

  override def create(orga: NewOrganisationEntity): Task[OrganisationEntity] =
    repo.zInsertReturning(orga)

  override def listAll(): Task[List[OrganisationEntity]] =
    repo.zFindAll.map(_.toList)
}

object OrganisationRepositoryLive {
  def layer: URLayer[DataSource, OrganisationRepository] =
    ZLayer.derive[OrganisationRepositoryLive]
}

given DbCodec[LatLon] = new DbCodec[LatLon] {

  def cols: IArray[Int]                                                                                       = IArray(java.sql.Types.JAVA_OBJECT)
  def queryRepr: String                                                                                       = "?"
  def readSingleOption(resultSet: java.sql.ResultSet, pos: Int): Option[org.worldofscala.organisation.LatLon] =
    val obj = resultSet.getObject(pos, classOf[PGpoint])
    if (resultSet.wasNull()) {
      None
    } else {
      val point = obj.asInstanceOf[PGpoint]
      Some(LatLon(point.x, point.y))
    }
  override def readSingle(rs: ResultSet, pos: Int): LatLon =
    val obj = rs.getObject(pos, classOf[PGpoint])
    if (rs.wasNull()) {
      LatLon.empty
    } else {
      val point = obj.asInstanceOf[PGpoint]
      LatLon(point.x, point.y)
    }
  override def writeSingle(entity: LatLon, ps: PreparedStatement, pos: Int): Unit =
    ps.setObject(pos, entity, java.sql.Types.OTHER)
}
