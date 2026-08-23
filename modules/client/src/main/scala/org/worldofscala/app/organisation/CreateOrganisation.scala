package org.worldofscala.organisation

import be.doeraene.webcomponents.ui5.*
import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*
import dev.cheleb.ziotapir.*
import dev.cheleb.ziotapir.laminar.*
import org.worldofscala.app.given
import org.worldofscala.auth.UserToken
import org.worldofscala.earth.MeshView
import org.worldofscala.earth.MeshEndpoint
import org.worldofscala.earth.MeshEntry

object CreateOrganisation extends SecuredContent[UserToken]:

  given Form[LatLonView] = stringFormWithValidation(using
    new Validator[LatLonView] {
      override def validate(value: String): Either[String, LatLonView] =
        value.split(",") match {
          case Array(lat, lon) =>
            (
              lat.toDoubleOption.toRight("Invalid latitude"),
              lon.toDoubleOption.toRight("Invalid longitude")
            ) match {
              case (Right(lat), Right(lon))           => Right(LatLonView(lat, lon))
              case (Left(latError), Left(rightError)) =>
                Left(s"$latError and $rightError")
              case (Left(latError), _) => Left(latError)
              case (_, Left(lonError)) => Left(lonError)
            }
          case _ => Left("Invalid format")
        }
    }
  )
  given Defaultable[LatLonView] with
    def default = LatLonView(46.5188, 6.5593)

  val meshes = EventBus[Seq[MeshEntry]]()

  override def init: Unit =
    MeshEndpoint.all(()).emit(meshes)
  def securedContent(token: UserToken) =
    val organisationVar = Var(
      NewOrganisationWiew("", LatLonView.empty, MeshView.default)
    )

    div(
      h1("Create  Organisation"),
      div(
        styleAttr := "float: left;",
        child <-- meshes.events.toSignal(Nil).map { meshes =>
          given Form[MeshView.Id] = selectMappedForm(meshes, m => m._1, m => m._2)
          organisationVar.asForm
        },
        children <-- organisationVar.signal.map {
          _.errorMessages.map(div(_)).toSeq
        }
      ),
      div(
        styleAttr := "max-width: fit-content; margin:1em auto",
        Button(
          "Create",
          disabled <-- organisationVar.signal.map(_.errorMessages.nonEmpty),
          onClick --> { _ =>
            // scalafmt:off

            OrganisationEndpoint
              .create(organisationVar.now())
              .run

            // scalafmt:on

          }
        )
      ),
      a(href := "mesh/new", "New mesh")
//      renderToast(organisationBus, errorBus)
    )

    // def renderToast(userBus: EventBus[Organisation], errorBus: EventBus[Throwable]) =
    //   Seq(
    //     Toast(
    //       cls := "srf-valid",
    //       _.duration  := 2.seconds,
    //       _.placement := ToastPlacement.MiddleCenter,
    //       child <-- userBus.events.map(renderUser),
    //       _.open <-- userBus.events.map(_ => true)
    //     ),
    //     Toast(
    //       cls := "srf-invalid",
    //       _.duration  := 2.seconds,
    //       _.placement := ToastPlacement.MiddleCenter,
    //       child <-- errorBus.events.map(_.getMessage()),
    //       _.open <-- errorBus.events.map(_ => true)
    //     )
    //   )
