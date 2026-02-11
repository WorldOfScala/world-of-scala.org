package org.worldofscala.repository

import zio.*
import zio.test.Assertion.*
import zio.test.*

import org.worldofscala.earth.*

object MeshRepositorySpec extends ZIOSpecDefault with RepositorySpec("sql/meshes.sql") {

  private val scalaMesh =
    NewMeshEntity(
      "Scala",
      Array.emptyByteArray
    )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("MeshRepositorySpec")(
      test("create a mesh") {
        val program = for {
          repository <- ZIO.service[MeshRepository]
          meshEntity <- repository.saveMesh(scalaMesh)

        } yield meshEntity

        program.map(meshEntity => assert(meshEntity.label)(equalTo(scalaMesh.label)))
      },
      test("list meshes") {
        val program = for {
          repository   <- ZIO.service[MeshRepository]
          _            <- repository.saveMesh(scalaMesh)
          meshEntities <- repository.listMeshes()
        } yield meshEntities

        program.map(meshEntities => assert(meshEntities.size)(equalTo(2)))
      }
    ).provide(
      MeshRepositoryLive.layer,
      dataSouurceLayer,
      Scope.default
    )

}
