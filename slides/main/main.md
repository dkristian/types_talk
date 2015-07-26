!SLIDE
# 1. Types 2. ??? 3. Profit!

Kristian Domagala

Melbourne Scala User Group

27th July, 2015


!SLIDE
# Pop Quiz
### What is tags?

```ruby
tags = item.tags
```

1. Collection of `Tag` object?
2. Collection of `String`s?
3. Comma delimited `String`?

!SLIDE
# All of the above!
Dependent on the code path

* From DB -> Tag collection
* From controller -> String

All paths tested, but tests were setup with path assumptions

<!-- Lucky we are working with a language that has compiler-checked types -->

!SLIDE
# Let's write some code
```scala
case class User(fName: String, lName: String)

def initials(u: User): (Char,Char) =
  (u.fName.head, u.lName.head)
```

!SLIDE
# Let's write some code
```scala
def initials(u: User): (Char, Char) =
  (u.fName.head, u.lName.head)

val bruce = User("Bruce", "Wayne")
initials(bruce)
// ('B','W')

val batman = User("Batman", "")
initials(batman)
// ...
```

!SLIDE
# What went wrong?
### Made an assumption about names

!SLIDE
# What happens...
## ...when you assume?

![types](main/when_you_assume.png)

<!--span class="ref">https://xkcd.com/1339/</span-->

#SUB https://xkcd.com/1339/

!SLIDE
![types](main/types.png)

<span class="ref">https://twitter.com/KenScambler/status/621933432365432832</span>

!SLIDE
# Maybe it works?

Maybe all the known code-paths that lead to that point have pre-validated names?

<!-- OOP, AOP -->

!SLIDE
# Coincidence Oriented Programming!

What happens when someone new comes on board and adds a new code path?

!SLIDE
# Let's encode an assumption
```scala
type Name = NonEmptyString

case class User(fName: Name, lName: Name)
```

!SLIDE
# NonEmptyString
```scala
case class NonEmptyString private (value: String) {
  def head = value.head
}

object NonEmptyString {
  def nonEmptyString(str: String): Option[NonEmptyString] =
    if (str.isEmpty) None
    else Some(NonEmptyString(str))

  def nonEmptyString(head: Char, tail: String): NonEmptyString =
    NonEmptyString(head + tail)
}
```

!SLIDE
# Let's try it again
```scala
type Name = NonEmptyString
case class User(fName: Name, lName: Name)

object User {
  def create(fName: String,lName: String): Option[User] =
    for {fn <- nonEmptyString(fName)
         ln <- nonEmptyString(lName)
    } yield User(fn,ln) }

  def initials(u: User): (Char, Char) = (u.fName.head, u.lName.head)
}
```

!SLIDE
# Profit!
```scala
val bruce:Option[User] = User.create("Bruce","Wayne")
// Some(User(...))
bruce.map(user => initials(user))
// Some(('B','W'))

val batman:Option[User] = User.create("Batman","")
// None
batman.map(user => initials(user))
// None
```

!SLIDE
# But wait

## Surely more code != Profit!?

How much extra code really?

* Reduced tests
* Reduced defensive programming

!SLIDE
# Just an extension

Writing a User class > using an associative array of properties to represent a user

So why stop at "primitives" when modeling your types

!SLIDE
# Boring!
### Strings are trivial; how does this help me in

## The Real World<span class="sup">TM</span>?

!SLIDE
# Real World Auth & Auth
```scala
trait Task { def id:Id; def noteId:Id; ... }
trait Note { def id:Id; def content:String; ... }
object Database {
  def loadTask(taskId:Id):Option[Task] = {
    // ensure that user is logged in
  }
  def loadNote(noteId:Id):Option[Note] = ...
  def updateNote(noteId:Id, newContent:String) = {
    // ensure that user is logged in
    // make sure note exists
    // check that note belongs to the logged in user
  }
}
```

!SLIDE
# Types
```scala
trait PasswordHash {
  def matches(other:PasswordHash):Boolean
}

trait Password object Password {
  def hash(pwd:NonEmptyString):PasswordHash = ...
}
trait SessionToken
```

!SLIDE
# ???
```scala
sealed trait AuthenticatedUser { def user:User } object Authentication {
  def auth(usr:User, pwd:NonEmptyString): Option[AuthenticatedUser] =
    if (Password.hash(pwd).matches(usr.pwdHash))
      Some(new AuthenticatedUser { val user = usr })
    else
      None

  def auth(usr:User, tok:SessionToken): Option[AuthenticatedUser] = ...
}
```

!SLIDE
# Authentication Profit!
```scala
trait Task { def id:Id; def noteId:Id; ... }
trait Note { def id:Id; def content:String; ... }
class Database(au:AuthenticatedUser) {
  def loadTask(taskId:Id):Option[Task] = {
    // can't call unless user has been authenticated
  }
  def loadNote(noteId:Id):Option[Note] = ...
  def updateNote(noteId:Id, newContent:String) = {
    // again, can't call unless user has been authenticated
    // still need to validate noteId...
  }
}
```

!SLIDE
# Authorisation Profit!
```scala
sealed trait Ref[A] { def id:Id }
trait Task extends Ref[Task] { def note:Ref[Note]; ...}
trait Note extends Ref[Note] { def content:String; ...}
class Database(au:AuthenticatedUser) {
  def loadTask(taskId:Id):Option[Task] = ...
  def loadNote(noteId:Id):Option[Note] = ...

  def updateNote(noteRef:Ref[Note], newContent:String) = {
    // can only get Ref[Note] from Database, which implies
    // the referred note belongs to the authenticated user
  }
}
```

!SLIDE
# More Profit?
Level of constraints depends on the sophistication of the type system

You can still rule-out a number of types of bugs and help focus your search when something does go wrong

!SLIDE

# Thanks!
##
* https://github.com/dkristian/types_profit
* http://kristian-domagala.blogspot.com.au/2009/04/using-type-system-for-discoverability.html
* https://twitter.com/kdomagala
* https://github.com/softprops/picture-show
