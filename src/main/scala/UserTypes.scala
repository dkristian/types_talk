package userTypes

// Slide width --------------------------------------------->

case class NonEmptyString private(value: String) {
  def init = value.head
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

case class User(firstName: Name, lastName: Name) {

  def initials: (Char, Char) =  (firstName.init, lastName.init)

}

object User {
  type Name = NonEmptyString

  def create(fName: String, lName: String): Option[User] =
    for {fn <- nonEmptyString(fName)
         ln <- nonEmptyString(lName)
    } yield User(fn, ln)

  def create(fName: Name, lName: Name) = User(fName, lName)
}

object Client {
import User._

val bruce: Option[User] = User.create("Bruce","Wayne")
// Some(User(Bruce, Wayne))
bruce.map(user => user.initials)
// Some(('B','W'))

val batman: Option[User] = User.create("Batman","")
// None
batman.map(user => user.initials)
// None

}
