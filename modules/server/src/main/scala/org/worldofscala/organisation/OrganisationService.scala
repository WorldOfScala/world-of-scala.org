package org.worldofscala.organisation

import dev.cheleb.ziochimney.*
import org.worldofscala.earth.Mesh
import org.worldofscala.user.User
import zio.*
import zio.stream.ZStream

trait OrganisationService {
  def create(organisation: NewOrganisation, userUUID: User.Id): Task[Organisation]
  def listAll(): Task[Seq[Organisation]]
  def streamAll(): ZStream[Any, Throwable, Organisation]

}

case class OrganisationServiceLive(organisationRepository: OrganisationRepository) extends OrganisationService {

  override def streamAll(): ZStream[Any, Throwable, Organisation] =
    organisationRepository
      .streamAll()
      .mapInto[Organisation]

  override def listAll(): Task[Seq[Organisation]] = organisationRepository
    .listAll()
    .mapInto[Organisation]

  override def create(organisation: NewOrganisation, userUUID: User.Id): Task[Organisation] =

    val organisationEntity =
      NewOrganisationEntity(
        createdBy = userUUID,
        name = organisation.name,
        location = organisation.location,
        meshId = Some(organisation.meshId).filterNot(_ == Mesh.default),
        creationDate = java.time.OffsetDateTime.now()
      )

    organisationRepository
      .create(organisationEntity)
      .mapInto[Organisation]

}

object OrganisationServiceLive:
  def layer: URLayer[OrganisationRepository, OrganisationService] = ZLayer.derive[OrganisationServiceLive]
