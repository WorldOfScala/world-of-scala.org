package org.worldofscala.repository

import zio.Task
import com.augustnagro.magnum.*

trait TransactionSupport(transactor: Transactor) {
  def tx[A](zio: Task[A]): Task[A] = transact(transactor)(zio.map(a => a))
}
