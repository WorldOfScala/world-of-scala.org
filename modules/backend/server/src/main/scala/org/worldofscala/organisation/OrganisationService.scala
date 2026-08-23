package org.worldofscala.organisation

import dev.cheleb.ziochimney.*
import io.scalaland.chimney.dsl.*
import org.worldofscala.earth.MeshView
import org.worldofscala.domain.user.User
import zio.*
import zio.stream.ZStream

trait OrganisationService {
  def create(organisation: NewOrganisationWiew, userUUID: User.Id): Task[OrganisationView]
  def listAll(): Task[Seq[OrganisationView]]
  def streamAll(): UIO[ZStream[Any, Throwable, OrganisationView]]

}

case class OrganisationServiceLive(organisationRepository: OrganisationRepository) extends OrganisationService {

  override def streamAll(): UIO[ZStream[Any, Throwable, OrganisationView]] =
    organisationRepository
      .streamAll()
      .map(_.mapInto[OrganisationView])

  override def listAll(): Task[Seq[OrganisationView]] = organisationRepository
    .listAll()
    .mapInto[OrganisationView]

  override def create(organisation: NewOrganisationWiew, userUUID: User.Id): Task[OrganisationView] =

    val organisationEntity =
      NewOrganisationEntity(
        createdBy = userUUID,
        name = organisation.name,
        location = organisation.location,
        meshId = Some(organisation.meshId).filterNot(_ == MeshView.default),
        creationDate = java.time.OffsetDateTime.now()
      )

    organisationRepository
      .create(organisationEntity)
      .map(_.into[OrganisationView].transform)

}

object OrganisationServiceLive:
  def layer: URLayer[OrganisationRepository, OrganisationService] = ZLayer.derive[OrganisationServiceLive]
