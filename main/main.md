!SLIDE
## 1. Types 2. ??? 3. Profit!

### Kristian Domagala

Melbourne Scala User Group, 27th July, 2015

!SLIDE
# My journey
## 

* 2000 &nbsp;Graduate Java developer - JSPs, Servlets
* 2002 &nbsp;EJBs!
* 2003 &nbsp;Hibernate/Spring
* 2004 &nbsp;Agile! TDD/BDD!
* 2007 &nbsp;Still looking for a better way
* 2008 &nbsp;Ruby on Rails!
* 2008 &nbsp;Scala (via Haskell)
* 2009 &nbsp;Java/Objective C
* 2010 &nbsp;Yeah, no, Scala

!SLIDE
# Pop Quiz

What is tags?

```ruby

  tags = item.tags


```

1. Collection of `Tag` object?
2. Collection of `String`s?
3. Comma delimited `String`?

!SLIDE
# All of the above!
Dependent on the code path

* From DB: `Tag` collection
* From controller: Comma-delimited `String`
* From some other code-path: collection of `String`s

All paths tested, but tests were setup with path assumptions

!SLIDE
# Let's write some code
```scala

case class User(firstName: String, lastName: String) {

  def initials: (Char,Char) = (firstName.head, lastName.head)

}

val bruce = User("Bruce", "Wayne")
bruce.initials
// ('B','W')

val batman = User("Batman", "")
batman.initials
// ...
```

!SLIDE
# Hmmmm
```scala
java.util.NoSuchElementException: next on empty iterator
	at scala.collection.Iterator$$anon$2.next(Iterator.scala:39)
	at scala.collection.Iterator$$anon$2.next(Iterator.scala:37)
	at scala.collection.IndexedSeqLike$Elements.next(IndexedSeqLike.scala:64)
	at scala.collection.IterableLike$class.head(IterableLike.scala:91)
	at scala.collection.immutable.StringOps.scala$collection$IndexedSeqOptimized$$super$head(StringOps.scala:31)
	at scala.collection.IndexedSeqOptimized$class.head(IndexedSeqOptimized.scala:120)
	at scala.collection.immutable.StringOps.head(StringOps.scala:31)
	at user.User$.initials(User.scala:7)
```

!SLIDE
# What went wrong?
### Made an assumption about names

!SLIDE
# What happens...
## ...when you assume?

![types](main/when_you_assume.png)

<span class="ref">https://xkcd.com/1339/</span>

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

case class User(firstName: Name, lastName: Name) {

  def initials: (Char, Char) = (firstName.init, lastName.init)

}
```

!SLIDE
# NonEmptyString
```scala
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
```

!SLIDE
# Let's try it again
```scala
type Name = NonEmptyString
case class User(firstName: Name, lastName: Name) {

  def initials: (Char, Char) = (firstName.init, lastName.init)

}

object User {

  def create(fName: String, lName: String): Option[User] =
    for {fn <- nonEmptyString(fName)
         ln <- nonEmptyString(lName)
    } yield User(fn, ln)

}
```

!SLIDE
# Profit!
```scala
val bruce: Option[User] = User.create("Bruce","Wayne")
// Some(User(Bruce, Wayne))
bruce.map(user => user.initials)
// Some(('B','W'))

val batman: Option[User] = User.create("Batman","")
// None
batman.map(user => user.initials)
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

Writing a `User` class in a typed language is an improvement over using an associative array of properties to represent a `User` in an untyped language

So why stop at "primitives" when modeling your types

<!-- Reference to item.tags example -->

!SLIDE
# Boring!
### Strings are trivial; how does this help me in

## The Real World<span class="sup">TM</span>?

!SLIDE
# Real World
```scala
trait Task { def id: Id; def noteId: Id; ... }
trait Note { def id: Id; def content: String; ... }

class Database(userId: Id) {

  // Need to ensure user is logged in
  def loadTask(taskId: Id): Option[Task] = ...
  def loadNote(noteId: Id): Option[Note] = ...

  // Need to ensure user is logged in
  // Need to ensure note exists and belongs to user
  def updateNote(noteId: Id, newContent: String) = ...
}
```

!SLIDE
# Types
```scala
case class User(userId: Id, pwdHash: PasswordHash)

trait PasswordHash {
  def matches(other: PasswordHash): Boolean
}

trait Password { def hash: PasswordHash }

trait SessionToken
```

!SLIDE
# ???
```scala
class AuthedUser private(val user: User)

object AuthedUser {

  def auth(usr: User, pwd: Password): Option[AuthedUser] =
    if (pwd.hash.matches(usr.pwdHash))
      Some(new AuthedUser(usr))
    else
      None

  def auth(usr:User, tok: SessionToken): Option[AuthedUser] =
    ...
}
```

!SLIDE
# Authentication Profit!
```scala
class Database(au: AuthedUser) {

  // Can't call unless user has been authenticated
  def loadTask(taskId: Id): Option[Task] = ...
  def loadNote(noteId: Id): Option[Note] = ...

  // Again, can't call unless user has been authenticated
  // Still need to check if note exists and is accessible
  def updateNote(noteId: Id, newContent: String) = ...
}
```

!SLIDE
# Authorisation Profit!
```scala

object Database {
  case class Task private(id: Id, noteId: Id, ...)
  case class Note private(id: Id, content: String, ...)
}

class Database(au: AuthedUser) {
  def loadTask(taskId: Id): Option[Task] = ...
  def loadNote(noteId: Id): Option[Note] = ...

  // Can only get Note from Database, which implies that:
  //   1. Note exists
  //   2. Note is accessible by AuthedUser
  def updateNote(note: Note, newContent: String) = ...
}
```

!SLIDE
# Generalisation Profit!

```scala
sealed trait Ref[A] { def id: Id }
object Database {
  case class Task private(id: Id, note: Ref[Note])
  case class Note private(id: Id, content: String)
    extends Ref[Note]
}
class Database(au: AuthedUser) {
  def loadTask(taskId: Id): Option[Task] = ...
  def loadNote(noteId: Id): Option[Note] = ...

  // Can only get Ref[Note] from Database (either from
  // loadNote or via loadTask)
  def updateNote(noteRef: Ref[Note], newContent: String) = ...
}
```

!SLIDE
# Types are cheap<span class="sup">*</span>
###### <span class="sup">*</span>Relative to the benefits they provide

!SLIDE
# Types are cheap
>Is there a type famine that I'm not aware of?

Rúnar Bjarnason

<span class="ref">https://groups.google.com/d/msg/scalaz/R-YYRoqzSXk/jk_gCJxMLoIJ</span>

!SLIDE
# More Profit?
Level of constraints depends on the sophistication of the type system

You can still rule-out a number of types of bugs and help focus your search when something does go wrong

!SLIDE
# Next time you ask...
![types](main/types.png)

<span class="ref">https://twitter.com/KenScambler/status/621933432365432832</span>

!SLIDE
# Thanks!

* <span class="ref">http://dkristian.github.io/types_talk</span>
* <span class="ref">http://kristian-domagala.blogspot.com.au/2009/04/using-type-system-for-discoverability.html</span>
* <span class="ref">https://twitter.com/kdomagala</span>
* <span class="ref">https://github.com/softprops/picture-show</span>
