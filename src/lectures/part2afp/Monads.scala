package lectures.part2afp

object Monads extends App {

  /** Intro
   *
   * Monads are kind of type that have some fundamental operations.
   */
  trait MonadTemplate[A] {
    def unit(value: A): MonadTemplate[A]                       // also called *pure* or *apply*
    def flatMap[B](f: A => MonadTemplate[B]): MonadTemplate[B] // also called *bind*
  }

  // List, Option, Try, Future, Stream, Set are all monads.

  /** Operations must satisfy the *monad laws*
   *
   * Left-identity: unit(x).flatMap(f) == f(x)
   * Right-identity: aMonadInstance.flatMap(unit) == aMonadInstance
   * Associativity: m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))
   */

  // Our own Try monad
  // encapsulates exceptions
  trait Attempt[+A] {
    def flatMap[B](f: A => Attempt[B]): Attempt[B]
  }
  object Attempt {
    // call-by-name because it may contain exceptions
    def apply[A](a: => A): Attempt[A] =
      try {
        Success(a)
      } catch {
        case ex: Throwable => Fail(ex)
      }
  }

  case class Success[+A](value: A) extends Attempt[A] {
    def flatMap[B](f: A => Attempt[B]): Attempt[B] =
      try {
        f(value)
      } catch {
        case ex: Throwable => Fail(ex)
      }
  }
  case class Fail(ex: Throwable) extends Attempt[Nothing] {
    def flatMap[B](f: Nothing => Attempt[B]): Attempt[B] = this
  }

  /*
    Proof of monad laws:

    left-identity:
      Attempt(x).flatMap(f)
      Fail(ex).flatMap(f) == Fail(ex)
      Success(v).flatMap(f) == f(v)

    right-identity:
      attempt.flatMap(apply)
      Fail(ex).flatMap(apply) == Fail(ex)
      Success(v).flatMap(apply) == Attempt(v) == Success(v)

    associativity:
      Fail(ex).flatMap(f).flatMap(g) == Fail(ex)
      Fail(ex).flatMap(x => f(x).flatMap(g)) == Fail(ex)
      Success(v).flatMap(f).flatMap(g) == f(v).flatMap(g) OR Fail(ex)
      Success(v).flatMap(x => f(x).flatMap(g)) == f(v).flatMap(g) OR Fail(ex) // same exception as above
   */

  val attempt = Attempt {
    throw new RuntimeException("Fail baby, fail!")
  }
  println(attempt)

  /*
  EXERCISES:
  1. implement a Lazy[T] monad = computation which will only be executed when it's needed

  2. Monads = unit + flatMap
     Monads = unit + map + flatten

     Monad[T] {

       def flatMap[B](f: T => Monad[B]): Monad[B] = ... (implemented)

       def map[B](f: T => B): Monad[B]
       def flatten[B](m: Monad[Monad[B]]): Monad[B]
   */

  class Lazy[+A](value: => A) {
    // call by need
    private lazy val internalVal = value // forces evaluation to only occur once!
    def use: A = internalVal
    def flatMap[B](f: ( => A) => Lazy[B]): Lazy[B] = f(internalVal)
  }
  object Lazy {
    def apply[A](x: => A): Lazy[A] = new Lazy[A](x)
  }

  val lazyInst = Lazy {
    println("Secret message")
    42
  } // no print
  // println(lazyInst.use) // prints "Secret message" and 42

  val fmLazyInst = lazyInst.flatMap(x => Lazy{
    10 * x
  })
  val fmLazyInst2 = lazyInst.flatMap(x => Lazy{
    10 * x
  })
  fmLazyInst.use
  fmLazyInst2.use // "Secret message" prints only once
}
