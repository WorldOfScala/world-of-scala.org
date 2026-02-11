package org.worldofscala

import sttp.tapir.Schema
import zio.json.JsonCodec
import zio.prelude.*
import zio.prelude.Debug.Repr

import java.util.UUID

trait UUIDOpaque[ID <: UUID](json: JsonCodec[ID], schema: Schema[ID]):
  given JsonCodec[ID] = json
  given Schema[ID]    = schema

  given Debug[ID] with
    def debug(value: ID): Repr = Repr.String(value.toString)
