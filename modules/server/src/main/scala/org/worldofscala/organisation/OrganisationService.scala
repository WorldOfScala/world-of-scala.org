package org.worldofscala.organisation

import zio.*
import zio.json.*

import io.scalaland.chimney.dsl.*
import zio.stream.ZStream
import org.worldofscala.user.User
import org.worldofscala.earth.Mesh
import org.worldofscala.repository.Repository

trait OrganisationService {
  def create(organisation: NewOrganisation, userUUID: User.Id): Task[Organisation]
  def listAll(): Task[List[Organisation]]
  def streamAll(): Task[ZStream[Any, Throwable, Byte]]

}

case class OrganisationServiceLive(organisationRepository: OrganisationRepository) extends OrganisationService {

  override def streamAll(): Task[ZStream[Any, Throwable, Byte]] =
    ZIO
      .succeed(
        organisationRepository
          .streamAll()
          .provideLayer(Repository.dataLayer)
          .flatMap(entity =>
            ZStream.fromIterable(
              (entity
                .into[Organisation]
                .transform
                .toJson + "\n").getBytes
            )
          )
      )

  override def listAll(): Task[List[Organisation]] = organisationRepository
    .listAll()
    .map(entities =>
      entities.map(entity =>
        entity
          .into[Organisation]
          .transform
      )
    )
    .provide(Repository.dataLayer)

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
      .map(entity =>
        entity
          .into[Organisation]
          .transform
      )
      .provide(Repository.dataLayer)
}

object OrganisationServiceLive:
  def layer: URLayer[OrganisationRepository, OrganisationService] = ZLayer.derive[OrganisationServiceLive]
