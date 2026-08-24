package dev.cheleb.ziochimney

import io.scalaland.chimney.Transformer
import zio.*
import zio.stream.ZStream

import scala.annotation.targetName

extension [R, E, A](zio: ZIO[R, E, A])
  inline transparent def mapInto[B](using transformer: Transformer.AutoDerived[A, B]): ZIO[R, E, B] =
    zio.map:
      transformer.transform

extension [R, A](zio: RIO[R, Option[A]])
  @targetName("mapIntoOption")
  inline transparent def mapInto[B](using transformer: Transformer.AutoDerived[A, B]): RIO[R, Option[B]] =
    zio.map:
      _.map:
        transformer.transform

extension [R, E, A](zio: ZIO[R, E, Seq[A]])
  @targetName("mapIntoList")
  def mapInto[B](using transformer: Transformer.AutoDerived[A, B]): ZIO[R, E, Seq[B]] =
    zio.map:
      _.map:
        transformer.transform

extension [R, A](zio: RIO[R, Vector[A]])
  @targetName("mapIntoVertor")
  inline transparent def mapInto[B](using transformer: Transformer[A, B]): RIO[R, Vector[B]] =
    zio.map:
      _.map:
        transformer.transform

extension [R, A](zstream: ZStream[Any, Throwable, A])
  inline transparent def mapInto[B](using
    transformer: Transformer.AutoDerived[A, B]
  ): ZStream[Any, Throwable, B] =
    zstream.map:
      transformer.transform
