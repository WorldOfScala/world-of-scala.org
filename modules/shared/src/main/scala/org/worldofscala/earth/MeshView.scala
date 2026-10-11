package org.worldofscala.earth

import sttp.tapir.Codec
import sttp.tapir.Schema
import zio.json.JsonCodec

import java.util.UUID
import org.worldofscala.domain.organisation.Mesh
import org.worldofscala.UUIDOpaque2

final case class MeshView(id: Mesh.Id, label: String, blob: Array[Byte], thumbnail: Option[String])
    derives JsonCodec,
      Schema

object MeshView extends UUIDOpaque2[Mesh.Id](Mesh.Id(_)):

  def default = Mesh.Id(UUID.fromString("00000000-0000-0000-0000-000000000000"))

  def defaulEntry = org.worldofscala.domain.organisation.MeshEntry(default, "Pinner", None, 0L)

  given Codec.PlainCodec[Mesh.Id] = Codec.parsedString(str => Mesh.Id(UUID.fromString(str)))

final case class MeshEntry(id: Mesh.Id, label: String, thumbnail: Option[String], count: Long) derives JsonCodec, Schema
object MeshEntry extends UUIDOpaque2[Mesh.Id](Mesh.Id(_))
