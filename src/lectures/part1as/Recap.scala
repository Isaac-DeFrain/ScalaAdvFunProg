package lectures.part1as

import scala.annotation.tailrec

object Recap extends App {

  val aCondition: Boolean = false
  val aConditionedVal = if (aCondition) -5 else 42
  // instructions vs expressions

  // compiler infers type
  val aCodeBlock = {
    if (aCondition) 56
    println(42)
  }

  // Unit = void
  val aUnit = println("Hello, Scala")

  // functions
  def aFunction(x: Int) = x + 1

  // recursion: stack vs tail
  @tailrec def factorial(n: Int, acc: Int): Int =
    if (n <= 1) acc
    else factorial(n - 1, n * acc)

  val fiveFactorial = factorial(5,1)
  println(fiveFactorial)
  /**
   * Tail recursion does not use additional stack frames when computing a function value.
   */

  // object-oriented programming
  class Plant
  class Animal
  class Grass extends Plant
  class Tomato extends Plant
  val aGrass: Plant = new Grass // subtyping polymorphism

  trait Herbivore {
    def eat(p: Plant): Unit
  }

  class Cow extends Animal with Herbivore {
    override def eat(p: Plant): Unit = println("Yum!")
  }

  // method notations
  val aCow = new Cow
  aCow eat aGrass // infix - equivalent to aCow.eat(aGrass)
  println(1.+(2)) // 1 + 2

  // anonymous classes
  /**
   * instantiates an anonymous class on the spot with supplied overridden method def
   */
  val aHerbivore = new Herbivore {
    override def eat(p: Plant): Unit = println("Yummy yummy!")
  }
  val aTomato = new Tomato
  aHerbivore eat aTomato

  // generics
  abstract class MyList[+A] //variance and variance problems in THIS course
  // singletons and companions
  object MyList

  // case classes
  /**
   * companions objects with apply methods
   * serializable
   * all parameters are actually fields
   */
  case class Person(age: String, name: Int)

  //exceptions/try/catch/finally
  //val throwableException = throw new RuntimeException // Nothing
  val aPotentialFailure: Unit = try {
    throw new RuntimeException
  } catch {
    case e: Exception => println("...I caught an exception")
  } finally {
    println("some logs")
  }

  // packaging and imports

  // functional programming
  /**
   * functions are instances of classes with apply method
   */
  val incrementer = new Function[Int, Int] {
    override def apply(x: Int): Int = x + 1
  }
  incrementer(2)

  val anonymousIncrementer = (x: Int) => x + 1 // lambda
  List(1,2,3).map(anonymousIncrementer) // HOF
  // map, flatmap, filter

  // for-comprehension = flatmap; map
  val pairs = for {
    num <- List(1,2,3)
    chr <- List('a','b','c')
  } yield num + "-" + chr
  println(pairs)

  // Scala collections: Seq, Set, List, Array, Vector, Map, Tuple, etc.
  val aMap = Map(
    "Isaac" -> List(1,2,3),
    "other" -> 2
  )
  println(aMap)

  // pseudo-collections: Option, Try
  val someOption = Some(42)

  // pattern matching
  val x = 2
  val order = x match {
    case 1 => "first"
    case 2 => "second"
    case 3 => "third"
    case _ => x + "th"
  }
  println(order)

  val isaac = Person("Isaac", 32)
  val ginger = new Plant
  val greeting1 = isaac match {
    case Person(n, a) => s"Hello, my name is $n. I am $a years old."
    case _ => "not a person" // exhausts all patterns so no MatchException is thrown
  }
  println(greeting1)
  println(ginger)
}
