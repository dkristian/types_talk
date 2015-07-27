package user

case class User(firstName: String, lastName: String) {

  def initials: (Char,Char) = (firstName.head, lastName.head)

}

object User {

val bruce = User("Bruce", "Wayne")
bruce.initials
// ('B','W')

val batman = User("Batman", "")
batman.initials
// ...
}
