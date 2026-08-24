package org.worldofscala.domain.organisation

import java.time.OffsetDateTime
import java.util.UUID
import org.worldofscala.domain.user.User

case class Organisation(
  id: Organisation.Id,
  name: String,
  location: LatLon,
  meshId: Option[Mesh.Id],
  createdBy: User.Id,
  creationDate: OffsetDateTime
)
object Organisation:

  opaque type Id <: UUID = UUID
  object Id:
    def apply(uuid: UUID): Id = uuid

case class LatLon(lat: Double, lon: Double)

object LatLon:
  val empty: LatLon = LatLon(0.0, 0.0)
