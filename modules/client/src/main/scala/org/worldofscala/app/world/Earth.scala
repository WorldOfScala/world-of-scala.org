package org.worldofscala.app.world

import org.scalajs.dom.window

import THREE.*

import com.raquo.laminar.api.L.*

import dev.cheleb.ziotapir.*
import dev.cheleb.ziotapir.laminar.*

import dev.cheleb.zthreesjs.*
import dev.cheleb.threesjs.*

import scala.scalajs.js.Math.PI

import org.worldofscala.organisation.Organisation
import org.worldofscala.organisation.OrganisationEndpoint
import zio.*
import org.worldofscala.organisation.LatLon
import dev.cheleb.ziotapir.BackendClientLive

import org.worldofscala.app.world.SceneHelper.*
import org.worldofscala.earth.Mesh.Id as MeshId
object Earth {

  val R = 1

  def apply() =

    val margin    = 0.88
    val eartthDiv = div()

    val scene  = new Scene();
    val camera = new PerspectiveCamera(30, window.innerWidth / window.innerHeight, 1, 100);

    camera.position.set(0, 0, 5)

    val renderer = WebGLRenderer(
      antialias = true,
      alpha = false
    )

    renderer.setPixelRatio(window.devicePixelRatio)
    renderer.setSize(window.innerWidth * margin, window.innerHeight * margin);

    val orbitControl = OrbitControls(camera, renderer.domElement)

    val detail = 300

    val geometry      = new IcosahedronGeometry(R - 0.05, 10)
    val pointGeometry = new IcosahedronGeometry(R, detail);

    val textureLoader = TextureLoader()

    val colorMap = textureLoader.load("/img/8081-earthmap10k.jpg")

    val material = MeshBasicMaterial(
      color = 0x555555,
      wireframe = true
    )

    val globeGroup = Group()

    val earth = new Mesh(geometry, material);

    globeGroup.add(earth)

    val pointMaterial = PointsMaterial(
      color = 0xf0f0f0,
      size = 0.02,
      map = colorMap
    )
    val points = Points(pointGeometry, pointMaterial)

    globeGroup.add(points)

    val loader = new GLTFLoader()

    loader.load(
      "//res/scala.glb",
      (obj) => {
        addPinner(globeGroup, obj, LatLon(46.5188, 6.5593)) // Lauzane
      }
    )

    OrganisationEndpoint
      .allStream(())
      .jsonlFoldZIO[Map[MeshId, GLTFResult], Organisation](Map.empty[MeshId, GLTFResult]):
        addOrganisationPinner(
          globeGroup,
          loader
        )

    scene.add(
      globeGroup
    )

    globeGroup.rotation.y = PI / 2
    globeGroup.rotation.x = PI / 4

    val animate: () => Unit = () => {

      // globeGroup.rotation.x += 0.001;
      // globeGroup.rotation.y += 0.0005;

      renderer.render(scene, camera);
      orbitControl.update()

    }
    renderer.setAnimationLoop(animate);

    val light = DirectionalLight(0xffffff, 100)

    light.position.set(5, 5, 5)
    light.lookAt(0, 0, 0)
    scene.add(light)

    eartthDiv.ref.append(renderer.domElement)

    window.addEventListener("resize", onWindowResize(camera, renderer, margin))

    eartthDiv

  def addOrganisationPinner(globeGroup: Group, loader: GLTFLoader)(
    cache: Map[MeshId, GLTFResult],
    organisation: Organisation
  ): ZIO[Any, Throwable, Map[MeshId, GLTFResult]] =
    val (meshId, meshUrl) = organisation.meshId match {
      case Some(id) =>
        (id, BackendClientLive.url("api", "mesh", id.toString()))
      case None =>
        (org.worldofscala.earth.Mesh.default, BackendClientLive.resourceUrl("res", "pinner.glb"))
    }

    val meshIO = cache.get(meshId) match {
      case Some(obj) =>
        ZIO.debug(s"Using cached mesh for ${organisation.name} with id ${meshId}") *>
          ZIO.succeed((false, obj))

      case None =>
        ZIO.debug(s"Loading mesh for ${organisation.name} with id ${meshId}") *>
          loader
            .zload(meshUrl)
            .map(obj => (true, obj))
            .mapError(error => new RuntimeException(s"Failed to load mesh for ${organisation.name}: ${error.message}"))

    }
    for {
      (addToCache, obj) <- meshIO
      _                 <- ZIO.debug(s"Addings a ${organisation.name} at ${organisation.location}")
      _                 <- ZIO.succeed(addPinner(globeGroup, obj, organisation.location))
    } yield
      if addToCache then cache + (meshId -> obj)
      else cache

  def addPinner(globeGroup: Group, obj: GLTFResult, location: LatLon) =
    val pinner    = obj.scene.jsClone(true)
    val (x, y, z) = location.xyz(R + 0.02)
    pinner.position.set(x, y, z)
    pinner.lookAt(0, 0, 0)
    globeGroup.add(pinner)
    globeGroup.add(drawLine(x, y, z))
}
