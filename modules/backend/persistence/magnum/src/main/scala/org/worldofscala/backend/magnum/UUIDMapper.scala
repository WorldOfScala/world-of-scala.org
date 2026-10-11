package org.worldofscala.backend.magnum

import com.augustnagro.magnum.*

import java.util.UUID

trait UUIDMapper[A](a2id: A => UUID, id2a: UUID => A) {
  given DbCodec[A] = DbCodec.UUIDCodec.biMap[A](id2a, a2id)
}
