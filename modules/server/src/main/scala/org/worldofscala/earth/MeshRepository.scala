package org.worldofscala.earth

import com.augustnagro.magnum.*

import org.worldofscala.earth.Mesh.*

import zio.*
import io.scalaland.chimney.dsl.*
import org.worldofscala.organisation.OrganisationEntity
import org.worldofscala.repository.UUIDMapper

trait MeshRepository:
  def get(id: Mesh.Id): Task[Option[MeshEntity]]
  def saveMesh(mesh: NewMeshEntity): Task[MeshEntity]
//   def deleteMesh(id: Mesh.Id): Unit
  def updateThumbnail(id: Mesh.Id, thumbnail: Option[String]): Task[Unit]
  def listMeshes(): Task[List[MeshEntry]]

object MeshRepository extends UUIDMapper[Mesh.Id](identity, Mesh.Id.apply)

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
case class NewMeshEntityRepo(
  id: Option[Mesh.Id],
  label: String,
  blob: Array[Byte]
) derives DbCodec

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
case class MeshEntityRepo(
  @Id id: Mesh.Id,
  label: String,
  blob: Array[Byte],
  thumbnail: Option[String]
) derives DbCodec

class MeshRepositoryLive private (val transactor: Transactor) extends MeshRepository:

  import MeshRepository.given

  transparent inline given TransformerConfiguration[?] =
    TransformerConfiguration.default.enableOptionDefaultsToNone

  override def saveMesh(mesh: NewMeshEntity): Task[MeshEntity] =
    transact(transactor) {
      val repo    = NewMeshEntityRepo(mesh.id, mesh.label, mesh.blob)
      val created = repo.create
      MeshEntity(created.id.get, created.label, created.blob, None)
    }

  override def updateThumbnail(id: Mesh.Id, thumbnail: Option[String]): Task[Unit] =
    transact(transactor) {
      sql"UPDATE meshes SET thumbnail = $thumbnail WHERE id = $id".update.run()
      ()
    }

  override def get(id: Id): Task[Option[MeshEntity]] =
    transact(transactor) {
      sql"SELECT id, label, blob, thumbnail FROM meshes WHERE id = $id"
        .query[MeshEntityRepo]
        .run()
        .headOption
        .map(_.map(r => MeshEntity(r.id, r.label, r.blob, r.thumbnail)))
    }

  override def listMeshes(): Task[List[MeshEntry]] =
    transact(transactor) {
      sql"""
        SELECT m.id, m.label, m.thumbnail, COUNT(o.id) as org_count
        FROM meshes m
        LEFT JOIN organisations o ON o.mesh_id = m.id
        GROUP BY m.id, m.label, m.thumbnail
        ORDER BY m.id
      """
        .query[(Mesh.Id, String, Option[String], Long)]
        .run()
        .map { case (id, label, thumbnail, count) =>
          MeshEntry(id, label, thumbnail, count.toInt)
        }
    }

object MeshRepositoryLive:
  def layer: URLayer[Transactor, MeshRepository] =
    ZLayer.derive[MeshRepositoryLive]
