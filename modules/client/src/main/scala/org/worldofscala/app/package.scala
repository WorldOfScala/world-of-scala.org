package org.worldofscala.app

import dev.cheleb.scalamigen.*
import dev.cheleb.scalamigen.ui5.UI5WidgetFactory
import dev.cheleb.ziotapir.*
import org.worldofscala.auth.UserToken
import org.worldofscala.user.Password

given Form[Password] = secretForm(Password(_))

given f: WidgetFactory = UI5WidgetFactory

given session: Session[UserToken] = SessionLive[UserToken]
