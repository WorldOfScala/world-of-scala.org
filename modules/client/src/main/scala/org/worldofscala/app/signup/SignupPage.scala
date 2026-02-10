package org.worldofscala.app.signup

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.ToastPlacement
import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*
import dev.cheleb.ziotapir.*
import dev.cheleb.ziotapir.laminar.*
import org.worldofscala.app.given
import org.worldofscala.user.*
import zio.prelude.*

import scala.concurrent.duration.DurationInt

extension (v: Signal[Boolean])
  def mapTrueToSome[A](a: A) = v.map:
    case true  => Some(a)
    case false => None

object SignupPage:
  def apply() =
    val personVar = Var(
      NewUser("John", "Does", "john.does@foo.bar", Password("notsecured"), Password("notsecured"))
    )
    val userBus  = EventBus[User]()
    val errorBus = EventBus[Throwable]()

    val debugVar = Var(false)

    div(
      h1("Signup"),
      div(
        styleAttr := "float: left;",
        //
        // The form is generated from the case class
        //
        personVar.asForm,
        children <-- personVar.signal.map {
          _.errorMessages.map(div(_)).toSeq
        }
      ),
      debugUI(debugVar, personVar),
      div(
        styleAttr := "max-width: fit-content; margin:1em auto",
        Button(
          "Create",
          disabled <-- personVar.signal.map(_.errorMessages.nonEmpty),
          onClick --> { _ =>
            // scalafmt:off

            UserEndpoint
              .create(personVar.now())
              .emit(userBus, errorBus)

            // scalafmt:on

          }
        )
      ),
      renderToast(userBus, errorBus)
    )

  def renderUser(user: User) =
    div(
      h2("User"),
      div(s"Id: ${user.id}"),
      div(s"Name: ${user.firstname}"),
      div(s"Name: ${user.lastname}"),
      div(s"Creation Date: ${user.creationDate}")
    )

  def debugUI(debugVar: Var[Boolean], personVar: Var[NewUser]) =
    div(
      styleAttr := "float: right;",
      Switch(
        _.textOn  := "🔎",
        _.textOff := "🔎",
        _.tooltip := "On/Off Switch",
        onChange.mapToChecked --> debugVar
      ),
      div(
        styleAttr := "float: both;",
        child.maybe <-- debugVar.signal.mapTrueToSome:
          div(
            styleAttr := "max-width: 300px; margin:1em auto",
            Title("Databinding"),
            child.text <-- personVar.signal.map(_.render)
          )
      )
    )

  def renderToast(userBus: EventBus[User], errorBus: EventBus[Throwable]) =
    Seq(
      Toast(
        cls := "srf-valid",
        _.duration  := 2.seconds,
        _.placement := ToastPlacement.MiddleCenter,
        child <-- userBus.events.map(renderUser),
        _.open <-- userBus.events.map(_ => true)
      ),
      Toast(
        cls := "srf-invalid",
        _.duration  := 2.seconds,
        _.placement := ToastPlacement.MiddleCenter,
        child <-- errorBus.events.map(_.getMessage()),
        _.open <-- errorBus.events.map(_ => true)
      )
    )
