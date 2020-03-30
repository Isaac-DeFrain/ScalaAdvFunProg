package exercises

import lectures.part4implicits.TypeClasses.{HTMLSerializer, User, UserSerializer}

object EqualityPlayground extends App {

  /*
 * Equality
 */
  trait Equal[T] {
    def apply(x: T, y: T): Boolean
  }

  object UserNameEqual extends Equal[User] {
    override def apply(x: User, y: User): Boolean = x.name == y.name
  }
  object UserAgeEqual extends Equal[User] {
    override def apply(x: User, y: User): Boolean = x.age == y.age
  }
  object UserEmailEqual extends Equal[User] {
    override def apply(x: User, y: User): Boolean = x.email == y.email
  }
  object UserEqual extends Equal[User] {
    override def apply(x: User, y: User): Boolean =
      UserAgeEqual(x, y) && UserNameEqual(x, y) && UserEmailEqual(x, y)
  }

  val quantifier = User("Quantifier", 666, "quantifiertech@gmail.com")
  println(UserSerializer.serialize(quantifier))

  val isaac = User("Isaac", 666, "someAddress@gmail.com")
  println(UserEqual(isaac, quantifier))

  /*
    Exercise: implement the TC pattern for the equality tc
   */
  object Equal {
    def apply[T](x: T, y: T)(implicit equal: Equal[T]) = equal(x, y)
  }

  implicit object UserNameAgeEqual extends Equal[User] {
    override def apply(x: User, y: User): Boolean = UserAgeEqual(x, y) && UserNameEqual(x, y)
  }

  val altI = User("Isaac", 666, "someOtherAddress@gmail.com")
  println(Equal(isaac, quantifier))
  println(Equal(isaac, altI))

  println(HTMLSerializer.serialize(isaac))

  // we now have access to the entire type class interface
  println(HTMLSerializer[User].serialize(isaac))

  /*
    Exercise: improve the Equal TC with implicit conversions
      ===(anotherValue: T)
      !==(anotherValue: T)
   */
  implicit class TypeSafeEqual[T](value: T) {
    def ===(anotherValue: T)(implicit equal: Equal[T]): Boolean = equal.apply(value, anotherValue)
    def !==(anotherValue: T)(implicit equal: Equal[T]): Boolean = !equal.apply(value, anotherValue)
  }

  println(isaac === altI)
  /*
    compiler does rewrites:
      * isaac.===(altI)
      * new TypeSafeEqual[User](isaac).===(altI)
      * new TypeSafeEqual[User](isaac).===(altI)(UserNameAgeEqual)
   */
  /*
    TYPE SAFE!
   */
  // println(isaac == 42)  -- valid syntax, compiles
  // println(isaac === 42) -- invalid syntax, does not compile!
}
