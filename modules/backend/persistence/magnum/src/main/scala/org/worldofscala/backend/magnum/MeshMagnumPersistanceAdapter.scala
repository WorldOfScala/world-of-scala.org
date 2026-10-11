package org.worldofscala.backend.magnum

import org.worldofscala.domain.organisation.{Mesh, MeshEntry}
import com.augustnagro.magnum.*
import com.augustnagro.magnum.ziomagnum.*
import io.scalaland.chimney.dsl.*
import dev.cheleb.ziochimney.*
import zio.*

import javax.sql.DataSource
import org.worldofscala.domain.organisation.MeshMagnumPersistancePort

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

object MeshEntity                                                            extends UUIDMapper[Mesh.Id](identity, Mesh.Id.apply)
class MeshMagnumPersistanceAdapterLive private (using DataSource, SqlLogger) extends MeshMagnumPersistancePort:

  import MeshEntity.given
  transparent inline given TransformerConfiguration[?] =
    TransformerConfiguration.default.enableOptionDefaultsToNone

  val repo = Repo[NewMeshEntity, MeshEntity, Mesh.Id]

  override def saveMesh(label: String, blob: Array[Byte]): Task[Mesh] =
    repo
      .zInsertReturning(NewMeshEntity(label, blob))
      .mapInto[Mesh]

  override def updateThumbnail(id: Mesh.Id, thumbnail: Option[String]): Task[Int] =
    sql"UPDATE meshes SET thumbnail = $thumbnail WHERE id = $id".zUpdate

  override def get(id: Mesh.Id): Task[Option[Mesh]] =
    repo
      .zFindById(id)
      .mapInto[Mesh]

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

object MeshMagnumPersistanceAdapterLive:
  def layer =
    ZLayer.derive[MeshMagnumPersistanceAdapterLive]
