package org.worldofscala.app

import com.raquo.laminar.api.L.*

import frontroute.*

import org.scalajs.dom

import org.worldofscala.*

object Router:
  private val externalUrlBus = EventBus[String]()
  val writer                 = externalUrlBus
  def apply()                =
    mainTag(
      linkHandler,
      routes(
        div(
          styleAttr := "max-width: fit-content;  margin-left: auto;  margin-right: auto;",
          // potentially children

          firstMatch(
            (pathEnd | path("index.html")) {
              world.Earth()
            },
            path("signup") {
              signup.SignupPage()
            },
            path("profile") {
              profile.ProfilePage()
            },
            pathPrefix("organisation") {
              firstMatch(
                path("new") {
                  organisation.CreateOrganisation()
                },
                path("mesh" / "new") {
                  organisation.CreateMesh()
                }
              )
            },
            path("about") {
              HomePage()
            }
          ),
          noneMatched:
            div("404 Not Found")
        )
      )
    )
  def linkHandler =
    onMountCallback(ctx => externalUrlBus.events.foreach(url => dom.window.location.href = url)(using ctx.owner))
