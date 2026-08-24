package org.worldofscala.organisation

import dev.cheleb.ziochimney.*
import io.scalaland.chimney.dsl.*

import org.worldofscala.domain.user.User
import zio.*
import zio.stream.ZStream
import org.worldofscala.domain.organisation.OrganisationPersistencePort
import org.worldofscala.domain.organisation.LatLon
import org.worldofscala.domain.organisation.Mesh

trait OrganisationService {
  def create(organisation: NewOrganisationWiew, userUUID: User.Id): Task[OrganisationView]
  def listAll(): Task[Seq[OrganisationView]]
  def streamAll(): UIO[ZStream[Any, Throwable, OrganisationView]]

}

case class OrganisationServiceLive(organisationRepository: OrganisationPersistencePort) extends OrganisationService {

  override def streamAll(): UIO[ZStream[Any, Throwable, OrganisationView]] =
    organisationRepository
      .streamAll()
      //  .map(_.map(_.transformInto[OrganisationView]))
      .map(_.mapInto[OrganisationView])

  override def listAll(): Task[Seq[OrganisationView]] = organisationRepository
    .listAll()
    //  .map(_.map(_.transformInto[OrganisationView]))
    .mapInto[OrganisationView]

  override def create(organisation: NewOrganisationWiew, userUUID: User.Id): Task[OrganisationView] =

    organisationRepository
      .create(
        createdBy = userUUID,
        name = organisation.name,
        location = organisation.location.transformInto[LatLon],
        meshId = Some(organisation.meshId).filterNot(_ == Mesh.default),
        creationDate = java.time.OffsetDateTime.now()
      )
      .mapInto[OrganisationView]

}

object OrganisationServiceLive:
  def layer: URLayer[OrganisationPersistencePort, OrganisationService] = ZLayer.derive[OrganisationServiceLive]
