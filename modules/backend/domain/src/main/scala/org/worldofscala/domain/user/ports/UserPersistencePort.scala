package org.worldofscala.domain.user.ports

import zio.*

import org.worldofscala.domain.user.*

trait UserPersistencePort {
  def create(user: NewUser): Task[User]
  def getById(id: User.Id): Task[Option[User]]
  def findByEmail(email: String): Task[Option[User]]
  def update(id: User.Id, op: User => User): Task[User]
  def delete(id: User.Id): Task[User]
}
