package org.worldofscala.domain.organisation

import zio.*
import zio.stream.ZStream

import org.worldofscala.domain.organisation.Organisation
import org.worldofscala.domain.organisation.Mesh
import org.worldofscala.domain.organisation.LatLon
import org.worldofscala.domain.user.User

trait OrganisationPersistencePort {
  def create(
    name: String,
    meshId: Option[Mesh.Id],
    location: LatLon,
    createdBy: User.Id,
    creationDate: java.time.OffsetDateTime
  ): Task[Organisation]
  def listAll(): Task[Seq[Organisation]]
  def streamAll(): UIO[ZStream[Any, Throwable, Organisation]]
}
