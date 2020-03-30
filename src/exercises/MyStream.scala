package exercises

import scala.annotation.tailrec

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
  def ++[B >: A](stream: => MyStream[B]): MyStream[B] // lazy stream concatenation

  def foreach(f: A => Unit): Unit
  def map[B](f: A => B): MyStream[B]
  def flatMap[B](f: A => MyStream[B]): MyStream[B]
  def filter(pred: A => Boolean): MyStream[A]

  def take(n: Int): MyStream[A] // picks out the first n elements of this stream, returns the stream containing these n elements
  def takeAsList(n: Int): List[A] = take(n).toList() // same as take, but returns a List

  @tailrec
  // Must be made final so subtypes of MyStream cannot override toList in a non-tailrec fashion
  final def toList[B >: A](acc: List[B] = Nil): List[B] = {
    if (isEmpty) acc.reverse
    else tail.toList(head :: acc)
  }
}

class Cons[A](hd: A, tl: => MyStream[A]) extends MyStream[A] {
  def isEmpty: Boolean = false

  override val head: A = hd
  override lazy val tail: MyStream[A] = tl // call by need tail implementation

  def #::[B >: A](elem: B): MyStream[B] = new Cons[B](elem, this)
  def ++[B >: A](stream: => MyStream[B]): MyStream[B] = new Cons(hd, tl ++ stream)

  def foreach(f: A => Unit): Unit = {
    f(hd)
    tl.foreach(f)
  }
  def map[B](f: A => B): MyStream[B] = new Cons(f(hd), tl.map(f))
  def flatMap[B](f: A => MyStream[B]): MyStream[B] = {
    f(hd) ++ tl.flatMap(f)
  }
  def filter(pred: A => Boolean): MyStream[A] = {
    if (pred(hd)) new Cons(hd, tl.filter(pred))
    else tl.filter(pred)
  }

  def take(n: Int): MyStream[A] = {
    if (n <= 0) EmptyStream
    else if (n == 1) new Cons(hd, EmptyStream)
    else new Cons(hd, tl.take(n - 1))
  }
}

object EmptyStream extends MyStream[Nothing] {
  def isEmpty: Boolean = true
  def head: Nothing = throw new NoSuchElementException
  def tail: MyStream[Nothing] = throw new NoSuchElementException

  def #::[B >: Nothing](elem: B): MyStream[B] = new Cons[B](elem, this)
  def ++[B >: Nothing](stream: => MyStream[B]): MyStream[B] = stream

  def foreach(f: Nothing => Unit): Unit = ()
  def map[B](f: Nothing => B): MyStream[B] = this
  def flatMap[B](f: Nothing => MyStream[B]): MyStream[B] = this
  def filter(pred: Nothing => Boolean): MyStream[Nothing] = this

  def take(n: Int): MyStream[Nothing] = this
}

object MyStream {
  def from[A](start: A)(gen: A => A): MyStream[A] = {
    new Cons(start, MyStream.from(gen(start))(gen))
  } // lazily generates stream from starting element
}

object StreamPlayground extends App {

  val naturals = MyStream.from(1)(_ + 1)
  println(naturals.head)
  println(naturals.tail.head)
  println(naturals.tail.tail.head)
  println(naturals.map(_ * 2).takeAsList(10))
  println((0 #:: naturals).take(1000).foreach(println)) // why is () printed?

  def divisible(m: Int, n: Int): Boolean = {
    if (m > 2 && n > 1) m % n == 0 || divisible(m, n - 1)
    else if (m < 2) true
    else false
  }
  val primes: MyStream[Int] = naturals.filter(x => !divisible(x, x-1))
  println(primes.takeAsList(15)) // List(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47)

  val flatMapped = naturals.flatMap(x => new Cons(x, new Cons(10*x, EmptyStream))) // StackOverflowError before making ++ call by name
  println(flatMapped.takeAsList(10)) // List(1, 10, 2, 20, 3, 30, 4, 40, 5, 50)

  println(naturals.filter(_ < 20).take(10).toList()) // List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  // println(naturals.filter(_ < 20).take(21).toList()) // StackOverflowError
  println(naturals.filter(_ < 20).take(10).take(20).toList()) // List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  // Collatz stream
  def collatz(n: Int): Int = {
    if (n % 2 == 0) n / 2
    else 3*n + 1
  }
  def collatzStream(n: Int): MyStream[Int] = {
    MyStream.from(n)(collatz)
  }
  println(collatzStream(7).takeAsList(25))

  // Fibonacci stream
  def fibonacci(m: BigInt, n: BigInt): MyStream[BigInt] = {
    new Cons[BigInt](m, fibonacci(n, m+n))
  }
  println(fibonacci(1,1).takeAsList(25))

  // Eratosthenes sieve
  def eratosthenes(nums: MyStream[Int]): MyStream[Int] = {
    if (nums.isEmpty) nums
    else if (nums.head < 2) nums.tail
    else new Cons(nums.head, eratosthenes(nums.tail.filter(_ % nums.head != 0)))
  }
  def sieve(n: Int, stream: MyStream[Int]): MyStream[Int] = {
    if (n == 0) stream
    else sieve(n-1, eratosthenes(stream))
  }
  println(sieve(4, naturals).takeAsList(15)) // List(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47)
  println(sieve(4, naturals).takeAsList(20) == primes.takeAsList(20)) // true
}
