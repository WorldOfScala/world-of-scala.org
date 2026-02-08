package dev.cheleb.ziochimney

import zio.*
import io.scalaland.chimney.Transformer
import scala.annotation.targetName
import zio.stream.ZStream

extension [R, A](zio: RIO[R, A])
  def mapInto[B](using transformer: Transformer[A, B]): RIO[R, B] =
    zio.map:
      transformer.transform

extension [R, A](zio: RIO[R, List[A]])
  @targetName("mapIntoList")
  def mapInto[B](using transformer: Transformer[A, B]): RIO[R, List[B]] =
    zio.map:
      _.map:
        transformer.transform

extension [R, A](zstream: ZStream[Any, Throwable, A])
  def mapInto[B](using transformer: Transformer[A, B]): ZStream[Any, Throwable, B] =
    zstream.map:
      transformer.transform
