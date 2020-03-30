package lectures.part1as

import scala.util.Try

object DarkSugars extends App {

  // syntax sugar #1: methods with single parameter
  def aSingleArgMethod(arg: Int) = s"There are $arg parallel universes."

  val description = aSingleArgMethod {
    // some complex code
    42
  }

  val aTryInstance = Try { // java's try
    throw new RuntimeException
  }

  val doubleListFilter = List(1,2,3,4,5).map {
    2 * _
  }.filter {
    _ > 5
  }
  println(doubleListFilter)

  // syntax sugar #2: single abstract method pattern
  /**
   * "normal" ways to instantiate trait:
   * - extend with non-abstract class
   * - implement an anonymous class
   */
  trait Action {
    def act(x: Int): Int
  }

  val anInstance: Action = (x: Int) => x + 1
  // equivalent to:
  // val anInstance: Action = new Action {
  //  override def act(x: Int): Int = x + 1 }

  // example: Runnables
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("Introducing, a new thread")
  })
  aThread.run()
  println(aThread)

  val aSweeterThread = new Thread(() => println("Introducing, a sweeter thread"))
  aSweeterThread.run()
  println(aSweeterThread)

  abstract class AnAbstractType {
    def implemented: Int = 42
    def f(x: Int): Unit // can be implemented with a lambda
  }

  val anAbstractInstance: AnAbstractType = x => println(x)
  anAbstractInstance.f(2)

  // syntax sugar #3: :: and #:: methods are special
  val prependList = 1 :: List(2,3,4,5) // equivalent to List(1).::(List(2,3,4,5))

  // Scala spec: associativity of a method is determined by last character of operator
  println(1 :: 2 :: List(3) == List(3).::(2).::(1)) // equivalent

  class MyStream[T] {
    def -->:(value: T) = this // actual implementation here - right associative
  }
  val aStream = 1 -->: 2 -->: 3 -->: new MyStream[Int]
  println(aStream)

  // syntax sugar #4: multi-word method naming
  class Deployer(id: String) {
    def `and then deployed`(deploy: String) = println(s"New deployment from $id: $deploy")
  }
  val deployer = new Deployer("Node")
  deployer `and then deployed` "Rholang code, boi"

  // syntax sugar #5: infix types
  class Composite[A, B]
  val composite: Int Composite String = new Composite[Int, String]

  class -->[A, B]
  val toTheRight: Int --> List[Int] = new (Int --> List[Int])
  println(toTheRight)

  // syntax sugar #6: update() - very special like apply()
  val anArray = Array(1,2,3)
  val anotherArray = (anArray(2) = 7) // used in mutable collections
  println(anotherArray == Array(1,2,3).update(2,7)) // true

  // syntax sugar #7: setters for mutable containers
  class Mutable {
    private var internalMember: Int = 0 // private for OO encapsulation
    def member = internalMember // "getter"
    def member_=(value: Int) = {
      internalMember = value // "setter"
    }
  }

  val aMutableContainer = new Mutable
  println((aMutableContainer.member = 42) == (aMutableContainer.member_=(42))) // true
}
