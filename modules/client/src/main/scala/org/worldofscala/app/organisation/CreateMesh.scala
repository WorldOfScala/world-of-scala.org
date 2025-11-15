package org.worldofscala.organisation

import com.raquo.laminar.api.L.*

import dev.cheleb.ziotapir.*
import dev.cheleb.ziotapir.laminar.*
import org.worldofscala.earth.MeshEndpoint
import org.scalajs.dom.window
import org.scalajs.dom

import java.io.*

import scala.scalajs.js.typedarray.*

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*

import org.worldofscala.earth.Mesh as OrgaMesh

import THREE.*
import org.worldofscala.earth.MeshEntry
import org.worldofscala.auth.UserToken
import org.worldofscala.app.given

object CreateMesh extends SecuredContent[UserToken]:

  val name                                       = Var("")
  val fileVar: Var[Option[org.scalajs.dom.File]] = Var(None)
  val thumbnail                                  = Var("")

  val meshes = EventBus[List[MeshEntry]]()

  private def reset() =
    name.set("")
    fileVar.set(None)
    thumbnail.set("")

  override def init: Unit = reset()

  def securedContent(token: UserToken) =
    div(
      styleAttr := "max-width: fit-content; margin:1em auto",
      div(
        styleAttr := "float: left; margin:1em",
        h1("Meshes"),
        allMeshes()
      ),
      div(
        styleAttr := "float: left; margin:1em",
        h2("Create Mesh"),
        div(
          styleAttr := "float: left;",
          "Name: ",
          input(
            nameAttr    := "name",
            placeholder := "Name",
            onInput.mapToValue --> name,
            value <-- name.signal
          )
        ),
        div(
          styleAttr := "float: left;",
          div(
            styleAttr := "float: left;",
            img(
              src <-- thumbnail.signal,
              width  := "100px",
              height := "100px"
            ),
            div(
              display <-- fileVar.signal.map(o => if o.isEmpty then "none" else "block"),
              div(
                h2("Preview"),
                child.maybe <-- fileVar.signal.map(preview)
              )
            )
          ),
          div(
            input(
              `type` := "file",
              accept := ".glb",
              onChange.mapToFiles --> {
                case file :: Nil =>
                  fileVar.set(Some(file))
                case _ =>
                  fileVar.set(None)

              }
            )
          ),
          div(
            Button(
              "Create",
              disabled <-- name.signal.map(_.isEmpty),
              onClick --> { ev =>
                ev.preventDefault()
                fileVar.now() match {
                  case Some(file) =>
                    file.arrayBuffer().`then` { buffer =>
                      val in  = new ByteArrayInputStream(new Int8Array(buffer).toArray)
                      val in2 = new ByteArrayInputStream(thumbnail.signal.now().getBytes())
                      val ios = for {
                        id <- MeshEndpoint.streamCreate(name.now(), in)
                        _  <- MeshEndpoint.putThumbnail(id, in2)
                        ls <- MeshEndpoint.all(())
                        _   = meshes.emit(ls)
                        _   = reset()
                      } yield id
                      ios.run
                    }
                  case None =>
                }
              }
            )
          )
        )
      )
    )

  def preview(fileOption: Option[org.scalajs.dom.File]) = fileOption match {
    case Some(file) =>
      val loader   = new GLTFLoader()
      val renderer = WebGLRenderer(
        preserveDrawingBuffer = true,
        antialias = true,
        alpha = false
      )

      var rotateX = 0.01d
      var rotateY = 0.01d
      val div2    = div(
        CheckBox(
          "Rotate",
          checked := true,
          onChange.mapToChecked --> { checked =>
            if (checked) {
              rotateX = 0.01;
              rotateY = 0.01;
            } else {
              rotateX = 0;
              rotateY = 0;
            }
          }
        )
      )
      val scene  = new Scene();
      val camera = new PerspectiveCamera(1, window.innerWidth / window.innerHeight, 1, 10);

      camera.position.set(1, 1, 2)

      renderer.setPixelRatio(window.devicePixelRatio)
      renderer.setSize(100, 100);

      val orbitControl = OrbitControls(camera, renderer.domElement)

      val animate: () => Unit = () => {

        scene.rotation.x += rotateX;
        scene.rotation.y += rotateY;

        renderer.render(scene, camera);
        orbitControl.update()

      }

      renderer.setAnimationLoop(animate);

      val light = DirectionalLight(0xffffff, 100)
//      val ambientLight = AmbientLight(0xffffff, 100)

      light.position.set(5, 5, 5)
      light.lookAt(0, 0, 0)
      scene.add(light)
      //    scene.add(ambientLight)

      val canvas = renderer.domElement

      div2.ref.append(canvas)

      div2.amend(
        Button(
          "Snap",
          onClick --> { _ =>
            val dataUrl = canvas.toDataURL("image/png")
            thumbnail.set(dataUrl)
          }
        )
      )

      file.arrayBuffer().`then` { buffer =>
        loader.parse(
          buffer,
          "",
          (gltf) => {
            scene.add(gltf.scene.jsClone(true))
          }
        )
      }
      Some(div2)
    case None => None
  }

  def loadAllMeshes(): Unit =
    MeshEndpoint.all(()).emit(meshes)

  def allMeshes() =

    div(
      onMountCallback { _ =>
        loadAllMeshes()
      },
      overflowY := "scroll",
      height    := "400px",
      Table(
        _.overflowMode    := TableOverflowMode.Scroll,
        _.slots.headerRow := Table.headerRow(
          _.cell(width := "20rem", span(lineHeight := "1.4rem", "ID")),
          _.cell(span(lineHeight := "1.4rem", "Name")),
          _.cell(span(lineHeight := "1.4rem", "Preview")),
          _.cell(span(lineHeight := "1.4rem", "Count"))
        ),
        children <-- meshes.events.map(
          _.map { case MeshEntry(id, name, thumbnail, count) =>
            Table.row(
              dataAttr("card-name") := id.toString,
              _.cell(id.toString()),
              _.cell(name),
              _.cell(
                thumbnail
                  .map(t =>
                    img(
                      src    := t,
                      width  := "64px",
                      height := "48px"
                    )
                  )
                  .getOrElse(span("No preview"))
              ),
              _.cell(count.toString())
            )
          }
        )
      )
    )
end CreateMesh
