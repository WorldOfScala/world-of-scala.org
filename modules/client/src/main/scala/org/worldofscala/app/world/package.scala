package org.worldofscala.app.world

import THREE.Object3D
import THREE.Sprite

import scalajs.js

class PinnerData(
  val id: Int,
  val city: String,
  var tooltip: Sprite
) extends js.Object:
  var pinner: js.UndefOr[Object3D] = js.undefined
