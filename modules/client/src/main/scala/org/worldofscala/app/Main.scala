package org.worldofscala.app

import com.raquo.laminar.api.L.*
import frontroute.LinkHandler
import org.scalajs.dom

@main def main: Unit =

  val myApp =
    div(
      onMountCallback(_ => session.loadUserState()),
      Header(),
      Router(),
      Footer()
    ).amend(LinkHandler.bind) // For interbal links

  val containerNode = dom.document.getElementById("app")
  render(containerNode, myApp)
