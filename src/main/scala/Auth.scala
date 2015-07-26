package auth

trait Id

trait Task { def id: Id; def noteId: Id; }
trait Note { def id: Id; def content: String; }

class Database(userId: Id) {
  // Need to ensure user is logged in and has access
  def loadTask(taskId: Id): Option[Task] = ???
  def loadNote(noteId: Id): Option[Note] = ???

  // Need to ensure user is logged in
  // Need to ensure note exists and belongs to user
  def updateNote(noteId: Id, newContent: String) = ???
}
