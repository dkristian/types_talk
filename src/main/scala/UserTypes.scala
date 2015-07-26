package userTypes

// Slide width --------------------------------------------->

case class NonEmptyString private(value: String) {
  def head = value.head
}

object NonEmptyString {
  def nonEmptyString(str: String): Option[NonEmptyString] =
    if (str.isEmpty) None
    else Some(NonEmptyString(str))

  def nonEmptyString(head: Char,
                     tail: String): NonEmptyString =
    NonEmptyString(head + tail)
}

import NonEmptyString.nonEmptyString
import User.Name

case class User(firstName: Name, lastName: Name)

object User {
  type Name = NonEmptyString

  def create(fName: String, lName: String): Option[User] =
    for {fn <- nonEmptyString(fName)
         ln <- nonEmptyString(lName)
    } yield User(fn, ln)

  def initials(u: User): (Char, Char) =
    (u.firstName.head, u.lastName.head)
}

object Client {
import User._

val bruce: Option[User] = User.create("Bruce","Wayne")
// Some(User(Bruce, Wayne))
bruce.map(user => initials(user))
// Some(('B','W'))

val batman: Option[User] = User.create("Batman","")
// None
batman.map(user => initials(user))
// None

}
