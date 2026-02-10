package dev.cheleb.zthreesjs

import THREE.*
import sttp.model.Uri
import zio.*

extension (loader: GLTFLoader)
  def zload(
    url: Uri
  ): IO[scala.scalajs.js.Error, GLTFResult] =
    ZIO.async { callback =>
      loader.load(
        url.toString,
        (gltf) => callback(ZIO.succeed(gltf)),
        onError = (error) => callback(ZIO.fail(error))
      )
    }
