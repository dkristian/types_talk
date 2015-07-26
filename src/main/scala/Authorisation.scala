package authorisation

import auth.Id
import authentication.AuthedUser

// Slide width --------------------------------------------->
import Database._

object Database {
  case class Task private(id: Id, noteId: Id)
  case class Note private(id: Id, content: String)
}
class Database(au: AuthedUser) {
  def loadTask(taskId: Id): Option[Task] = ???
  def loadNote(noteId: Id): Option[Note] = ???

  // Can only get Note from Database, which implies that:
  //   1. Note exists
  //   2. Note is accessible by AuthedUser
  def updateNote(noteRef: Note, newContent: String) = ???
}
