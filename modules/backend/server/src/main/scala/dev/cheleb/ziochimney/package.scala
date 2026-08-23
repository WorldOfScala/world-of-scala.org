package dev.cheleb.ziochimney

import io.scalaland.chimney.Transformer
import zio.*
import zio.stream.ZStream

import scala.annotation.targetName

extension [R, A](zio: RIO[R, A])
  def mapInto[B](using transformer: Transformer[A, B]): RIO[R, B] =
    zio.map:
      transformer.transform

extension [R, A](zio: RIO[R, Seq[A]])
  @targetName("mapIntoList")
  def mapInto[B](using transformer: Transformer[A, B]): RIO[R, Seq[B]] =
    zio.map:
      _.map:
        transformer.transform

extension [R, A](zstream: ZStream[Any, Throwable, A])
  def mapInto[B](using transformer: Transformer[A, B]): ZStream[Any, Throwable, B] =
    zstream.map:
      transformer.transform
