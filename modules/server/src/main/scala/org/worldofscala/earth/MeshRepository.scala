package org.worldofscala.earth

import com.augustnagro.magnum.*
import com.augustnagro.magnum.ziomagnum.*
import io.scalaland.chimney.dsl.*
import org.worldofscala.repository.UUIDMapper
import zio.*

import javax.sql.DataSource

trait MeshRepository:
  def get(id: Mesh.Id): Task[Option[MeshEntity]]
  def saveMesh(mesh: NewMeshEntity): Task[MeshEntity]
//   def deleteMesh(id: Mesh.Id): Unit
  def updateThumbnail(id: Mesh.Id, thumbnail: Option[String]): Task[Int]
  def listMeshes(): Task[Vector[MeshEntry]]

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("meshes")
case class NewMeshEntity(
  label: String,
  blob: Array[Byte]
) derives DbCodec

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("meshes")
case class MeshEntity(
  @Id id: Mesh.Id,
  label: String,
  blob: Array[Byte],
  thumbnail: Option[String]
) derives DbCodec

object MeshEntity                                   extends UUIDMapper[Mesh.Id](identity, Mesh.Id.apply)
class MeshRepositoryLive private (using DataSource) extends MeshRepository:

  import MeshEntity.given
  transparent inline given TransformerConfiguration[?] =
    TransformerConfiguration.default.enableOptionDefaultsToNone

  val repo = Repo[NewMeshEntity, MeshEntity, Mesh.Id]

  override def saveMesh(mesh: NewMeshEntity): Task[MeshEntity] =
    repo.zInsertReturning(mesh)

  override def updateThumbnail(id: Mesh.Id, thumbnail: Option[String]): Task[Int] =
    sql"UPDATE meshes SET thumbnail = $thumbnail WHERE id = $id".zUpdate

  override def get(id: Mesh.Id): Task[Option[MeshEntity]] =
    repo.zFindById(id)

  private given DbCodec[MeshEntry] = DbCodec.derived[MeshEntry]

  override def listMeshes(): Task[Vector[MeshEntry]] =
    sql"""
        SELECT m.id, m.label, m.thumbnail, COUNT(o.id) as org_count
        FROM meshes m
        LEFT JOIN organisations o ON o.mesh_id = m.id
        GROUP BY m.id, m.label, m.thumbnail
        ORDER BY m.id
      """
      .zQuery[MeshEntry]

object MeshRepositoryLive:
  def layer: URLayer[DataSource, MeshRepository] =
    ZLayer.derive[MeshRepositoryLive]
