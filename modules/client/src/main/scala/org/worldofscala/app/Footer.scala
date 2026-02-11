package org.worldofscala.app

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.BarDesign
import com.raquo.laminar.api.L.*
import org.worldofscala.BuildInfo

object Footer:
  def apply(): HtmlElement =
    div(styleAttr := "clear:both", Bar(_.design := BarDesign.Footer, s"World Of Scala - v${BuildInfo.version}"))
