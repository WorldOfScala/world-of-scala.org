package org.worldofscala.earth

import zio.*
import java.io.InputStream
import org.worldofscala.earth.Mesh.Id
import io.scalaland.chimney.dsl._

trait MeshService:
  def createStream(name: String, stream: InputStream): Task[Mesh.Id]
  def updateThumnail(id: Mesh.Id, thumbnail: InputStream): Task[Mesh.Id]
  def get(id: Mesh.Id): Task[Mesh]
  def listAll(): Task[Seq[MeshEntry]]

case class MeshServiceLive(meshRepository: MeshRepository) extends MeshService {

  override def get(id: Id): Task[Mesh] = meshRepository
    .get(id)
    .someOrFail(new Exception("Mesh not found"))
    .map(_.into[Mesh].transform)

  def createStream(name: String, stream: InputStream): Task[Mesh.Id] =
    val newMeshEntity = NewMeshEntity(name, stream.readAllBytes())
    meshRepository
      .saveMesh(newMeshEntity)
      .map(_.id)

  def updateThumnail(id: Id, thumbnail: InputStream): Task[Mesh.Id] =
    meshRepository
      .updateThumbnail(id, Some(String(thumbnail.readAllBytes())))
      .map(_ => id)

  def listAll(): Task[Seq[MeshEntry]] =
    meshRepository
      .listMeshes()
      .map(meshes => Mesh.defaulEntry +: meshes)

}

object MeshServiceLive:
  def layer: ZLayer[MeshRepository, Nothing, MeshService] = ZLayer.derive[MeshServiceLive]
