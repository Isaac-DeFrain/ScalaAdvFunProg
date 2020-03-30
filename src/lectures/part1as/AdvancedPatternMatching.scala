package lectures.part1as

import lectures.part1as.Recap.MyList

object AdvancedPatternMatching extends App {

  val numbers = List(1)
  val description = numbers match {
    case hd :: Nil => println(s"The only element in the list is $hd.")
    case _ => println("The list doesn't have only one element.")
  }

  /** Structures available for pattern matching:
   * constants
   * wildcards
   * case classes
   * tuples
   * infix operators
   * any object with an unapply method defined (see what follows)
   */

  // Suppose this cannot be made a case class (for whatever reason), but we still want to do pattern matching
  class Person(val name: String, val age: Int)
  // Define companion object with special method unapply : thing we want to match -> Option[thing we want to return from match]
  // need not share the name of class, but is best practice
  object Person {
    def unapply(person: Person): Option[(String, Int)] = Some((person.name, person.age))

    def unapply(age: Int): Option[String] = {
      Some(if (age >= 21) "adult" else "minor")
    }
  }

  val bob = new Person("Bob", 42)
  val greeting = bob match {
    case Person(n, a) => s"Hey yo! My name is $n and I'm $a yo."
    case _            => "What is it?"
//    case List(_)      => "Really, a list!?"
  }
  println(greeting)

  val legalStatus = bob.age match {
    case Person(status) if status == "adult" => s"I am an $status!"
    case Person(status) if status == "minor" => s"I am a $status..."
  }
  println(legalStatus)

  // Exercise.
  // my first attempt
  class Number(val num: Int)
  object Number {
    def unapply(n: Number): Option[String] = Some(
      if (n.num < 10) "single digit"
      else if (n.num % 2 == 0) "even number"
           else "something else"
    )
  }
  val number = new Number(12)
  val numMatch = number match {
    case Number(prop) => prop
  }
  println(numMatch)

  // Daniel's solution
  object singleDigit {
    def unapply(n: Int): Boolean = n > -10 && n < 10
  }

  object even {
    def unapply(n: Int): Boolean = n % 2 == 0
  }

  val n = 45
  val mathProperty = n match {
    case singleDigit() => "single digit"
    case even()        => "even number"
    case _             => "something else"
  }
  println(mathProperty)

  // infix patterns
  case class Or[A, B](a: A, b: B)
  val either = Or(2, "two")
  val humanDescription = either match {
    case number Or string => s"$number is written as $string"
  }
  println(humanDescription)

  // decomposing sequences
  val vararg = numbers match {
    case List(1, _*) => "The list starts with 1"
  }

  abstract class MyList[+A] {
    def head: A = ???
    def tail: MyList[A] = ???
  }
  case object Empty extends MyList[Nothing]
  case class Cons[+A](override val head: A, override val tail: MyList[A]) extends MyList[A]

  object MyList {
    def unapplySeq[A](list: MyList[A]): Option[Seq[A]] = {
      if (list == Empty) Some(Seq.empty)
      else unapplySeq(list.tail).map(list.head +: _)
    }
  }
  val myList: MyList[Int] = Cons(1, Cons(2, Cons(3, Empty)))
  val decomposed = myList match {
    case MyList(1, 2, _*) => "staring with 1, 2"
    case _                => "something else"
  }
  println(decomposed)

  // custom return types for unapply
  /** Return types need to have these two methods:
   * isEmpty: Boolean
   * get: something
   */

  abstract class Wrapper[T] {
    def isEmpty: Boolean
    def get: T
  }
  
  object PersonWrapper {
    def unapply(person: Person): Wrapper[String] = new Wrapper[String] {
      def isEmpty = false
      def get = person.name
    }
  }
  println(bob match {
    case PersonWrapper(n) => s"His name is $n."
    case _                => "He's an alien."
  })
}
