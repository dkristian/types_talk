package authentication

import auth.{Id, Task, Note}

trait PasswordHash {
  def matches(other: PasswordHash): Boolean
}

trait SessionToken

trait Password { def hash: PasswordHash }

case class User(userId: Id, pwdHash: PasswordHash)

class AuthedUser private(val user: User)

object AuthedUser {

  def auth(usr: User, pwd: Password): Option[AuthedUser] =
    if (pwd.hash.matches(usr.pwdHash))
      Some(new AuthedUser(usr))
    else
      None

  def auth(usr:User, tok: SessionToken): Option[AuthedUser] =
???
}

class Database(au: AuthedUser) {

  // Can't call unless user has been authenticated
  def loadTask(taskId: Id): Option[Task] = ???
  def loadNote(noteId: Id): Option[Note] = ???

  // Again, can't call unless user has been authenticated
  // Still need to check if note exists and is accessible
  def updateNote(noteId: Id, newContent: String) = ???
}
