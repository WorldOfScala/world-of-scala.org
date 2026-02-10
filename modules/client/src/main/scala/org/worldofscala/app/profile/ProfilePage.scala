package org.worldofscala.app.profile

import com.raquo.laminar.api.L.*
import dev.cheleb.ziotapir.*
import dev.cheleb.ziotapir.laminar.*
import org.worldofscala.app.given
import org.worldofscala.auth.UserToken
import org.worldofscala.user.*

/**
 * ProfilePage is a secured page that displays the user's profile information.
 */
object ProfilePage extends SecuredContent[UserToken]:

  val userBus = new EventBus[User]

  override def init =
    UserEndpoint.profile(()).emit(userBus)
  def securedContent(userToken: UserToken) =
    div(
      h1("Profile Page"),
      child <-- userBus.events.map { user =>
        div(
          cls := "srf-form",
          h2("User"),
          div("First name: ", user.firstname),
          div("Last name: ", user.lastname),
          div("Email: ", user.email)
        )
      }
    )
