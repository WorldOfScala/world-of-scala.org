package org.worldofscala.domain.organisation

import java.util.UUID

final case class Mesh(id: Mesh.Id, label: String, blob: Array[Byte], thumbnail: Option[String])
final case class MeshEntry(id: Mesh.Id, label: String, thumbnail: Option[String], count: Long)

opaque type Id <: UUID = UUID

object Mesh:

  def default = Mesh.Id(UUID.fromString("00000000-0000-0000-0000-000000000000"))

  def defaulEntry = MeshEntry(default, "Pinner", None, 0L)

  opaque type Id <: UUID = UUID
  object Id:
    def apply(uuid: UUID): Id = uuid

  extension (id: Id) {
    def <(that: UUID) = id.toString < that.toString
  }
