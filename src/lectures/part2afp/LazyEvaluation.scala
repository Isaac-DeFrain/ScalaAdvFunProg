package lectures.part2afp

object LazyEvaluation extends App {

  // Program immediately crashes
  // val x: Int = throw new RuntimeException

  // But this does not crash the program!
  // Lazy values are evaluated only once when they are called the first time.
  // lazy DELAYS the evaluation
  lazy val x: Int = throw new RuntimeException

  lazy val y: Int = {
    println("Hello!")
    42
  }
  println(y) // Prints Hello! and 42
  println(y) // Only prints 42

  // Examples of implications
  // side effects
  def sideEffectCond: Boolean = {
    println("Boo")
    true
  }
  def simpleCond: Boolean = false
  lazy val lazyCond = sideEffectCond
  // This is a bad example... semantics of if...else... cause this behavior, not lazy evaluation
  println(if (simpleCond && lazyCond) "yes" else "no") // only prints no
  println(if (lazyCond && simpleCond) "yes" else "no") // prints Boo, then no

  // in conjunction with call by name
  def byNameMethod(n: => Int): Int = {
    // CALL BY NEED
    lazy val v = n // only evaluated once
    v + v + v + 1
  }
  def retrieveMagicalValue = {
    // side effect or long computation
    println("waiting")
    Thread.sleep(1000)
    42
  }
  println(byNameMethod(retrieveMagicalValue)) // waiting only prints once even though byNameMethod calls it's arg by name

  // filtering with lazy vals
  def lessThan30(i: Int): Boolean = {
    println(s"Is $i less than 30?")
    i < 30
  }
  def greaterThan20(i: Int): Boolean = {
    println(s"Is $i greater than 20?")
    i > 20
  }

  println("-------- Call by value --------")
  val numbers = List(1, 25, 40, 27)
  val lt30 = numbers.filter(lessThan30) // Remember: compiler applies eta-expansion
  val gt20 = lt30.filter(greaterThan20)
  println(gt20)

  println("-------- Call by need --------")
  val lt30lazy = numbers.withFilter(lessThan30)
  val gt20lazy = lt30lazy.withFilter(greaterThan20)
  gt20lazy.foreach(println)

  // for-comprehensions use withFilter with guards
  val forVal = for {
    a <- List(1,2,3,4) if a % 2 == 0 // use lazy vals
  } yield a + 1 // equivalent to List(1,2,3,4).withFilter(_ % 2 == 0).map(_ + 1)
  println(forVal)

  /*
    Exercise: implement a lazily evaluated, singly linked STREAM of elements

    naturals = MyStream.from(1)(_ + 1) // stream of natural numbers (potentially infinite)
    naturals.take(100).foreach(println) // lazily evaluated stream of first 100 naturals (finite stream)
    naturals.foreach(println) // infinite => will crash!
    naturals.map(_ * 2) // stream of all even natural numbers (potentially infinite)
   */
  abstract class MyStream[+A] { // covariant
    def isEmpty: Boolean
    def head: A
    def tail: MyStream[A]

    def #::[B >: A](elem: B): MyStream[B] // prepend operator
    def ++[B >: A](stream: MyStream[B]): MyStream[B]

    def foreach(f: A => Unit): Unit
    def map[B](f: A => B): MyStream[B]
    def flatMap[B](f: A => MyStream[B]): MyStream[B]
    def filter(pred: A => Boolean): MyStream[A]

    def take(n: Int): MyStream[A] // picks out the first n elements of this stream, returns the stream containing these n elements
    def takeAsList(n: Int): List[A] // same as take, but returns a List
  }

  object MyStream {
    def from[A](start: A)(gen: A => A): MyStream[A] = ??? // generates stream from starting element
  }
}
