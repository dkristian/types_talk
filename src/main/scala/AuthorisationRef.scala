package authorisationRef

import auth.Id
import authentication.AuthedUser

// Slide width --------------------------------------------->
import Database._

sealed trait Ref[A] { def id: Id }
object Database {
  case class Task private(id: Id, note: Ref[Note])
  case class Note private(id: Id, content: String)
    extends Ref[Note]
}
class Database(au: AuthedUser) {
  def loadTask(taskId: Id): Option[Task] = ???
  def loadNote(noteId: Id): Option[Note] = ???

  // Can only get Ref[Note] from Database (either from
  // loadNote or via loadTask)
  def updateNote(noteRef: Ref[Note], newContent: String) = ???
}
