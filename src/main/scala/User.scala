package user

case class User(firstName: String, lastName: String)

object User {
  def initials(u: User): (Char,Char) =
    (u.firstName.head, u.lastName.head)
}
