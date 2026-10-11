package org.worldofscala

import sttp.tapir.Schema
import zio.json.JsonCodec
import zio.prelude.*
import zio.prelude.Debug.Repr

import java.util.UUID
import sttp.tapir.SchemaType.SString

trait UUIDOpaque[ID <: UUID](json: JsonCodec[ID], schema: Schema[ID]):
  given JsonCodec[ID] = json
  given Schema[ID]    = schema

  given Debug[ID] with
    def debug(value: ID): Repr = Repr.String(value.toString)

trait UUIDOpaque2[ID <: UUID](f: UUID => ID):
  given JsonCodec[ID] = JsonCodec.uuid.transform(
    uuid => f(uuid),
    id => id
  )
  given Schema[ID] = Schema(SString[ID]()).format("uuid")
  given Debug[ID] with
    def debug(value: ID): Repr = Repr.String(value.toString)

trait UUIDOpaque_2[ID <: UUID](f: UUID => ID):
  given jsB: JsonCodec[ID] = JsonCodec.uuid.transform(
    uuid => f(uuid),
    id => id
  )
  given tsB: Schema[ID] = Schema(SString[ID]()).format("uuid")
  given dbB: Debug[ID] with
    def debug(value: ID): Repr = Repr.String(value.toString)
