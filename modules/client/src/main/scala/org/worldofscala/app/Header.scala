package org.worldofscala.app

import zio.json.*

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.*

import dev.cheleb.scalamigen.*

import dev.cheleb.ziotapir.*
import dev.cheleb.ziotapir.laminar.*

import scala.concurrent.duration.DurationInt
import org.worldofscala.auth.LoginPassword
import org.worldofscala.user.Password
import org.worldofscala.auth.UserToken
import org.worldofscala.user.UserEndpoint

object Header:
  private val openPopoverBus = new EventBus[Boolean]
  private val profileId      = "profileId"

  private val loginErrorEventBus   = new EventBus[Throwable]
  private val loginSuccessEventBus = new EventBus[Unit]

  val credentials = Var(LoginPassword("", Password("")))

  given Form[Password] = secretForm(Password(_))

  def apply(): HtmlElement =
    div(
      ShellBar(
        _.slots.startButton := a(
          Icon(_.name := IconName.home, cls := "pad-10"),
          href := "/"
        ),
        _.primaryTitle   := "World Of Scala",
        _.secondaryTitle := "Scala ❤️ around the world",
        _.slots.profile  := Avatar(idAttr := profileId, img(src := "img/scala.svg")),
        _.events.onProfileClick.mapTo(true) --> openPopoverBus
      ).amend(
        aria.label := "World of Scala header"
      ),
      Popover(
        _.openerId := profileId,
        _.open <-- openPopoverBus.events
          .mergeWith(loginSuccessEventBus.events.mapTo(false)),
        div(
          Title("Sign in"),
          child <-- session(notLogged)(logged)
        )
      )
    )

  def notLogged =
    div(
      cls := "pad-10",
      credentials.asForm,
      Toast(
        cls := "srf-invalid",
        _.duration  := 2.seconds,
        _.placement := ToastPlacement.MiddleCenter,
        child <-- loginErrorEventBus.events.map(_.getMessage()),
        _.open <-- loginErrorEventBus.events.mapTo(true)
      ),
      div(
        cls := "center",
        Button(
          "Login",
          disabled <-- credentials.signal.map(_.isIncomplete),
          onClick --> loginHandler(session)
        )
      ),
      a("Sign up", href := "/signup")
        .amend(
          onClick.mapTo(false) --> openPopoverBus
        )
    )

  def logged(userToken: UserToken) =
    UList(
      _.separators := ListSeparator.None,
      _.item(
        _.icon := IconName.settings,
        a("Settings", href := "/profile", title := s" Logged in as ${userToken.email}")
      )
        .amend(
          onClick.mapTo(false) --> openPopoverBus
        ),
      _.item(a("Organisation", href := "organisation/new")),
      _.item(_.icon := IconName.log, "Sign out").amend(
        onClick --> { _ =>
          session.clearUserState()
          openPopoverBus.emit(false)
        }
      )
    )

  def loginHandler(session: Session[UserToken]): Observer[Any] = Observer[Any] { _ =>
    UserEndpoint
      .login(credentials.now())
      .map(token => session.saveToken(token.toJson))
      .emit(loginSuccessEventBus, loginErrorEventBus)
  }
