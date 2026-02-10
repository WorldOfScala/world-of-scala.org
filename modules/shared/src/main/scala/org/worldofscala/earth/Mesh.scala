package org.worldofscala.earth

import org.worldofscala.UUIDOpaque
import sttp.tapir.Codec
import sttp.tapir.Schema
import zio.json.JsonCodec

import java.util.UUID

final case class Mesh(id: Mesh.Id, label: String, blob: Array[Byte]) derives JsonCodec, Schema
final case class MeshEntry(id: Mesh.Id, label: String, thumbnail: Option[String], count: Long) derives JsonCodec, Schema

object Mesh:

  def default = Mesh.Id(UUID.fromString("00000000-0000-0000-0000-000000000000"))

  def defaulEntry = MeshEntry(default, "Scala", None, 0L)

  opaque type Id <: UUID = UUID

  object Id extends UUIDOpaque[Id](JsonCodec.uuid, Schema.schemaForUUID):
    def apply(uuid: UUID): Id = uuid

    given Codec.PlainCodec[Id] = Codec.uuid

  extension (id: Id) {
    def <(that: UUID) = id.toString < that.toString
  }
