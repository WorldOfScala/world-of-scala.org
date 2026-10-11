package org.worldofscala.earth

import io.scalaland.chimney.dsl.*
import dev.cheleb.ziochimney.*
import zio.*

import java.io.InputStream
import org.worldofscala.domain.organisation.Mesh
import org.worldofscala.domain.organisation.MeshMagnumPersistancePort

trait MeshService:
  def createStream(name: String, stream: InputStream): Task[Mesh.Id]
  def updateThumnail(id: Mesh.Id, thumbnail: InputStream): Task[Mesh.Id]
  def get(id: Mesh.Id): Task[MeshView]
  def listAll(): Task[Seq[MeshEntry]]

case class MeshServiceLive(meshRepository: MeshMagnumPersistancePort) extends MeshService {

  override def get(id: Mesh.Id): Task[MeshView] = meshRepository
    .get(id)
    .someOrFail(new Exception("Mesh not found"))
    .mapInto[MeshView]

  def createStream(name: String, stream: InputStream): Task[Mesh.Id] =
    meshRepository
      .saveMesh(name, stream.readAllBytes())
      .map(_.id)

  def updateThumnail(id: Mesh.Id, thumbnail: InputStream): Task[Mesh.Id] =
    meshRepository
      .updateThumbnail(id, Some(String(thumbnail.readAllBytes())))
      .map(_ => id)

  def listAll(): Task[Seq[MeshEntry]] =
    meshRepository
      .listMeshes()
      .map(meshes => Mesh.defaulEntry +: meshes)
      .mapInto[MeshEntry]

}

object MeshServiceLive:
  def layer: ZLayer[MeshMagnumPersistancePort, Nothing, MeshService] = ZLayer.derive[MeshServiceLive]
