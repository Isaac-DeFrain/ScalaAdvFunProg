package exercises

import scala.annotation.tailrec

trait MySet[A] extends (A => Boolean) {

  // A functional set.
  override def apply(elem: A): Boolean = contains(elem)
  def contains(elem: A): Boolean
  def +(elem: A): MySet[A]
  def ++(set: MySet[A]): MySet[A]
  def map[B](f: A => B): MySet[B]
  def flatMap[B](f: A => MySet[B]): MySet[B]
  def filter(pred: A => Boolean): MySet[A]
  def forEach(f: A => Unit): Unit
  def -(elem: A): MySet[A]
  def &(set: MySet[A]): MySet[A]
  def --(set: MySet[A]): MySet[A]
  def unary_! : MySet[A]
}

/*
 MySet implementation separates EmptySet and NonEmptySet class definitions.
 */
// EmptySet[A] class is a subtype of MySet[A]
class EmptySet[A] extends MySet[A] {
  def contains(elem: A): Boolean = false
  def +(elem: A): MySet[A] = new NonEmptySet[A](elem, this)
  def ++(set: MySet[A]): MySet[A] = set
  def map[B](f: A => B): MySet[B] = new EmptySet[B]
  def flatMap[B](f: A => MySet[B]): MySet[B] = new EmptySet[B]
  def filter(pred: A => Boolean): MySet[A] = this
  def forEach(f: A => Unit): Unit = ()
  def -(elem: A): MySet[A] = this
  def &(set: MySet[A]): MySet[A] = this
  def --(set: MySet[A]): MySet[A] = this
  def unary_! : MySet[A] = new PropertyBasedSet[A](_ => true)
}

// NonEmptySet[A] is also a subtype of MySet[A]
// * two parameters - head: A, rest: MySet[A]
class NonEmptySet[A](val head: A, val rest: MySet[A]) extends MySet[A] {
  override def apply(elem: A): Boolean = contains(elem)
  def +(elem: A): MySet[A] =
    if (this.contains(elem)) this
    else new NonEmptySet[A](elem, this)
  def ++(set: MySet[A]): MySet[A] =
    rest ++ set + head
  def contains(elem: A): Boolean =
    head == elem || rest.contains(elem)
  def map[B](f: A => B): MySet[B] =
    rest.map(f) + f(head)
  def flatMap[B](f: A => MySet[B]): MySet[B] =
    f(head) ++ rest.flatMap(f)
  def filter(pred: A => Boolean): MySet[A] = {
    val filteredRest = rest.filter(pred)
    if (pred(head)) filteredRest + head
    else filteredRest
  }
  def forEach(f: A => Unit): Unit = {
    f(head)
    rest.forEach(f)
  }
  // Added in Enhancing Functional Sets
  def -(elem: A): MySet[A] = {
    if (head == elem) rest
    else rest - elem + head
  }
  def &(set: MySet[A]): MySet[A] = {
    // apply == contains
    filter(set(_))
    // intersection == filter
  }
  def --(set: MySet[A]): MySet[A] = {
    //filter(!set(_))
    filter(!set)
  }
  def unary_! = new PropertyBasedSet[A](!this.contains(_))
}
/* Not a great way to define a potentially infinite set
class AllInclusiveSet[A] extends MySet[A] {
  def contains(elem: A): Boolean = true
  def +(elem: A): MySet[A] = this
  def ++(set: MySet[A]): MySet[A] = this
  def map[B](f: A => B): MySet[B] = ???
  def flatMap[B](f: A => MySet[B]): MySet[B] = ???
  def filter(pred: A => Boolean): MySet[A] = filter(!pred(_))
  def forEach(f: A => Unit): Unit = ???
  def -(elem: A): MySet[A] = this - elem
  def &(set: MySet[A]): MySet[A] = filter(set)
  def --(set: MySet[A]): MySet[A] = filter(!set)
  def unary_! : MySet[A] = new EmptySet[A]
}
*/

// Better to use a property based set.
class PropertyBasedSet[A](property: A => Boolean) extends MySet[A] {
  def contains(elem: A): Boolean = property(elem)
  def +(elem: A): MySet[A] =
    new PropertyBasedSet[A](x => property(x) || x == elem)
  def ++(set: MySet[A]): MySet[A] =
    new PropertyBasedSet[A](x => property(x) || set.contains(x))
  def map[B](f: A => B): MySet[B] = politelyFail
  def flatMap[B](f: A => MySet[B]): MySet[B] = politelyFail
  def forEach(f: A => Unit): Unit = politelyFail
  def filter(pred: A => Boolean): MySet[A] =
  new PropertyBasedSet[A](x => pred(x) && property(x))
  def -(elem: A): MySet[A] =
    new PropertyBasedSet[A](filter(_ != elem))
  def &(set: MySet[A]): MySet[A] =
    new PropertyBasedSet[A](filter(set(_)))
  def --(set: MySet[A]): MySet[A] =
    new PropertyBasedSet[A](filter(!set(_)))
  def unary_! : MySet[A] =
    new PropertyBasedSet[A](!property(_))

  def politelyFail = throw new IllegalArgumentException("Really deep rabbit hole!")
}

// MySet object has an apply method (taking varargs) and calls an tailrec aux function buildSet to "build" the set
object MySet {
  def apply[A](values: A*): MySet[A] = {
    @tailrec
    def buildSet(valSeq: Seq[A], acc: MySet[A]): MySet[A] = {
      if (valSeq.isEmpty) acc
      else buildSet(valSeq.tail, acc + valSeq.head)
    }
    buildSet(values.toSeq, new EmptySet[A])
  }
}

// Testing our implementation.
object MySetPlayground extends App {
  val mySet = MySet(1,2,3,4)
  mySet + 5 + 3 ++ MySet(0,6) map (_ * 10) filter (_ < 35) flatMap (x => MySet(x, x / 10)) filter (_ % 2 == 0) forEach println

  val negative = !mySet
  println(negative(2))   // false
  println(negative(666)) // true :)

  val negativeOdd = negative.filter(_ % 2 == 1)
  println(negativeOdd(666))

  val negativeOdd666 = negativeOdd + 666
  println(negativeOdd666(666))
}