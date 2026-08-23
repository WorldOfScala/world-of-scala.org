package org.worldofscala.earth

import io.scalaland.chimney.dsl.*
import org.worldofscala.earth.MeshView.Id
import zio.*

import java.io.InputStream

trait MeshService:
  def createStream(name: String, stream: InputStream): Task[MeshView.Id]
  def updateThumnail(id: MeshView.Id, thumbnail: InputStream): Task[MeshView.Id]
  def get(id: MeshView.Id): Task[MeshView]
  def listAll(): Task[Seq[MeshEntry]]

case class MeshServiceLive(meshRepository: MeshRepository) extends MeshService {

  override def get(id: Id): Task[MeshView] = meshRepository
    .get(id)
    .someOrFail(new Exception("Mesh not found"))
    .map(_.into[MeshView].transform)

  def createStream(name: String, stream: InputStream): Task[MeshView.Id] =
    val newMeshEntity = NewMeshEntity(name, stream.readAllBytes())
    meshRepository
      .saveMesh(newMeshEntity)
      .map(_.id)

  def updateThumnail(id: Id, thumbnail: InputStream): Task[MeshView.Id] =
    meshRepository
      .updateThumbnail(id, Some(String(thumbnail.readAllBytes())))
      .map(_ => id)

  def listAll(): Task[Seq[MeshEntry]] =
    meshRepository
      .listMeshes()
      .map(meshes => MeshView.defaulEntry +: meshes)

}

object MeshServiceLive:
  def layer: ZLayer[MeshRepository, Nothing, MeshService] = ZLayer.derive[MeshServiceLive]
