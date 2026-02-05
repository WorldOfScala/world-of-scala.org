package org.worldofscala.organisation

import com.augustnagro.magnum.*
import org.worldofscala.*
import io.scalaland.chimney.dsl.*
import org.worldofscala.user.User
import org.worldofscala.earth.Mesh

import zio.*
import zio.stream.ZStream
import org.worldofscala.repository.PGpointSupport

import org.worldofscala.user.UserRepository
import org.worldofscala.earth.MeshRepository

import org.worldofscala.repository.UUIDMapper

trait OrganisationRepository {
  def create(org: NewOrganisationEntity): Task[OrganisationEntity]
  def listAll(): Task[List[OrganisationEntity]]
  def streamAll(): ZStream[Any, Throwable, OrganisationEntity]
}

object OrganisationRepository extends UUIDMapper[Organisation.Id](identity, Organisation.Id.apply)

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
case class NewOrganisationEntityRepo(
  id: Option[Organisation.Id] = None,
  createdBy: User.Id,
  name: String,
  meshId: Option[Mesh.Id] = None,
  location: LatLon,
  creationDate: Option[java.time.ZonedDateTime] = None
) derives DbCodec

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
case class OrganisationEntityRepo(
  @Id id: Organisation.Id,
  createdBy: User.Id,
  name: String,
  meshId: Option[Mesh.Id],
  location: LatLon,
  creationDate: java.time.ZonedDateTime
) derives DbCodec

class OrganisationRepositoryLive private (val transactor: Transactor)
    extends OrganisationRepository
    with PGpointSupport {

  import OrganisationRepository.given
  import MeshRepository.given
  import UserRepository.given

  override def streamAll(): ZStream[Any, Throwable, OrganisationEntity] =
    ZStream.fromIterableZIO {
      listAll()
    }

  override def create(orga: NewOrganisationEntity): Task[OrganisationEntity] =
    transact(transactor) {
      val repo =
        NewOrganisationEntityRepo(orga.id, orga.createdBy, orga.name, orga.meshId, orga.location, orga.creationDate)
      val created = repo.create
      OrganisationEntity(
        created.id.get,
        created.createdBy,
        created.name,
        created.meshId,
        created.location,
        created.creationDate.get
      )
    }

  override def listAll(): Task[List[OrganisationEntity]] =
    transact(transactor) {
      sql"SELECT id, created_by, name, mesh_id, location, creation_date FROM organisations"
        .query[OrganisationEntityRepo]
        .run()
        .map(r => OrganisationEntity(r.id, r.createdBy, r.name, r.meshId, r.location, r.creationDate))
    }
}

object OrganisationRepositoryLive {
  def layer: URLayer[Transactor, OrganisationRepository] =
    ZLayer.derive[OrganisationRepositoryLive]
}
