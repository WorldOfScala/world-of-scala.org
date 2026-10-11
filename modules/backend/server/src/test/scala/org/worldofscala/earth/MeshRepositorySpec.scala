package org.worldofscala.repository

import zio.*
import zio.test.Assertion.*
import zio.test.*

import com.augustnagro.magnum.ziomagnum.Slf4jMagnumLogger
import org.worldofscala.backend.magnum.MeshMagnumPersistanceAdapterLive
import org.worldofscala.domain.organisation.MeshMagnumPersistancePort

object MeshRepositorySpec extends ZIOSpecDefault with RepositorySpec("sql/meshes.sql") {

  private val scalaMeshLabel                            = "scalaMesh"
  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("MeshRepositorySpec")(
      test("create a mesh") {
        val program = for {
          repository <- ZIO.service[MeshMagnumPersistancePort]
          meshEntity <- repository.saveMesh(scalaMeshLabel, Array.emptyByteArray)

        } yield meshEntity

        program.map(meshEntity => assert(meshEntity.label)(equalTo(scalaMeshLabel)))
      },
      test("list meshes") {
        val program = for {
          repository   <- ZIO.service[MeshMagnumPersistancePort]
          _            <- repository.saveMesh(scalaMeshLabel, Array.emptyByteArray)
          meshEntities <- repository.listMeshes()
        } yield meshEntities

        program.map(meshEntities => assert(meshEntities.size)(equalTo(2)))
      }
    ).provide(
      Slf4jMagnumLogger.live(),
      MeshMagnumPersistanceAdapterLive.layer,
      dataSourceLayer,
      Scope.default
    )

}
