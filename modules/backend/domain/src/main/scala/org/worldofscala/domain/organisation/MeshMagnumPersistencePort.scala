package org.worldofscala.domain.organisation

import zio.*

trait MeshMagnumPersistancePort:
  def get(id: Mesh.Id): Task[Option[Mesh]]
  def saveMesh(label: String, blob: Array[Byte]): Task[Mesh]
//   def deleteMesh(id: Mesh.Id): Unit
  def updateThumbnail(id: Mesh.Id, thumbnail: Option[String]): Task[Int]
  def listMeshes(): Task[Vector[MeshEntry]]
