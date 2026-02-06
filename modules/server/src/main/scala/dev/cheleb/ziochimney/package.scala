package dev.cheleb.ziochimney

import zio.*
import io.scalaland.chimney.Transformer

extension [R, A](zio: RIO[R, A]) {
  def mapInto[B](using transformer: Transformer[A, B]): RIO[R, B] =
    zio.map(a => transformer.transform(a))
}
