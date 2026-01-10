package org.worldofscala.app

import com.raquo.laminar.api.L.*

import frontroute.*

import org.scalajs.dom

import org.worldofscala.*
import zio.ZIO
import org.scalajs.dom.HTMLElement
import com.raquo.laminar.nodes.ReactiveHtmlElement

object Router:
  /*
     This bus is used to handle external links, e.g. when a user clicks on a link.
   */
  private val externalUrlBus = EventBus[String]()

  def goTo(uri: String)  = externalUrlBus.emit(uri)
  def zGoTo(uri: String) = ZIO.succeed(Router.writer.emit(uri))

  private val writer = externalUrlBus

  /**
   * The router is a function that takes a path and returns a component.
   */
  def apply(): ReactiveHtmlElement[HTMLElement] =
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
