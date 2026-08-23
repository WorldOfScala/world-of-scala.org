package org.worldofscala.organisation

import dev.cheleb.scalamigen.NoPanel
import org.worldofscala.UUIDOpaque
import org.worldofscala.earth.MeshView

import org.worldofscala.user.given
import sttp.tapir.Schema
import zio.json.JsonCodec
import zio.prelude.*
import zio.prelude.Debug.Repr
import zio.prelude.magnolia.*

import java.time.OffsetDateTime
import java.util.UUID

import Math.{cos, sin, PI}
import org.worldofscala.domain.user.User
import sttp.tapir.SchemaType.SString

@NoPanel
case class NewOrganisationWiew(
  name: String,
  location: LatLonView,
  meshId: MeshView.Id
) derives JsonCodec,
      Schema,
      Debug:
  def errorMessages: Seq[String] = Seq.empty

/**
 * LatLon
 *
 * @param lat
 * @param lon
 */
@NoPanel
case class LatLonView(lat: Double, lon: Double) derives JsonCodec, Schema, Debug:
  override def toString(): String = s"$lat,$lon"

  /**
   * Convert lat lon to xyz
   *
   * @param d
   *   earth radius
   * @return
   */
  def xyz(d: Double) = {
    val latRad = lat * PI / 180.0;
    val lonRad = (-lon + 180) * PI / 180.0;
    (d * cos(latRad) * cos(lonRad), d * sin(latRad), d * cos(latRad) * sin(lonRad));

  }

object LatLonView:
  val empty: LatLonView = LatLonView(0.0, 0.0)

case class OrganisationView(
  id: OrganisationView.Id,
  name: String,
  location: LatLonView,
  meshId: Option[MeshView.Id],
  createdBy: User.Id,
  creationDate: OffsetDateTime
) derives JsonCodec,
      Schema,
      Debug:
  def errorMessages: Seq[String] = Seq.empty

object OrganisationView:
  given Debug[OffsetDateTime] with
    def debug(value: OffsetDateTime): Repr = Repr.String(value.toString)

  given Debug[org.worldofscala.domain.user.User.Id] with
    def debug(value: org.worldofscala.domain.user.User.Id): Repr = Repr.String(value.toString)

  given JsonCodec[User.Id] = JsonCodec.uuid.transform(
    uuid => User.Id(uuid),
    id => id
  )
  given Schema[User.Id] =
    Schema(SString[User.Id]()).format("uuid")

  opaque type Id <: UUID = UUID

  object Id extends UUIDOpaque[Id](JsonCodec.uuid, Schema.schemaForUUID):
    def apply(uuid: UUID): Id = uuid
