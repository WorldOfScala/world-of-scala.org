package org.worldofscala.app

import dev.cheleb.scalamigen.*
import dev.cheleb.scalamigen.ui5.UI5WidgetFactory
import org.worldofscala.auth.UserToken
import org.worldofscala.user.Password
import dev.cheleb.ziotapir.laminar.LaminarSessionLive
import dev.cheleb.ziotapir.laminar.LaminarSession

given Form[Password] = secretForm(Password(_))

given f: WidgetFactory = UI5WidgetFactory

given session: LaminarSession[UserToken] = LaminarSessionLive[UserToken]
