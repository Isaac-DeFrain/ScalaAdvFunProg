package lectures.part4implicits

import scala.concurrent.Future

object MagnetPattern extends App {

  // helps with issues caused by method overloading

  class P2PRequest

  class P2PResponse

  class Serializer[T]

  trait Actor {
    def receive(statusCode: Int): Int // return type is not important
    def receive(statusCode: P2PRequest): Int
    def receive(statusCode: P2PResponse): Int
    //def receive[T](message: T)(implicit serializer: Serializer[T]): Int
    def receive[T: Serializer](message: T): Int // context-bound instead
    def receive[T: Serializer](message: T, statusCode: Int): Int
    def receive(future: Future[P2PRequest]): Int
    //def receive(future: Future[P2PResponse]): Int // does not compile because of type erasure
    // lots of other overloads
  }

  /*
    Problems with this style:
    1. type erasure
    2. higher-order functions
      - lifting doesn't work for all overloads
    3. code duplication
      - likely similar implementations
    4. type inference and default args
      actor.receive(!?)
   */

  trait MessageMagnet[Result] {
    def apply(): Result
  }

  def receive[R](message: MessageMagnet[R]): R = message()

  implicit class FromP2PRequest(request: P2PRequest) extends MessageMagnet[Int] {
    def apply(): Int = {
      // logic from handling a P2PRequest
      println("Handling P2P request")
      42
    }
  }

  implicit class FromP2PResponse(response: P2PResponse) extends MessageMagnet[Int] {
    def apply(): Int = {
      // logic from handling a P2PResponse
      println("Handling P2P response")
      666
    }
  }

  // Now we can simply do
  receive(new P2PRequest)   // implicit conversion: P2PRequest  -> FromP2PRequest(P2PRequest)
  receive(new P2PResponse)  // implicit conversion: P2PResponse -> FromP2PResponse(P2PResponse)

  /*
    Benefits of magnet pattern:
    1. no more type erasures
    2. lifting works -- with a catch
   */

  // 1. no more type erasures
  implicit class FromFutureP2PRequest(response: Future[P2PRequest]) extends MessageMagnet[Int] {
    def apply(): Int = {
      // logic from handling a P2PResponse
      println("Handling P2P future request")
      1337
    }
  }
  implicit class FromFutureP2PResponse(response: Future[P2PResponse]) extends MessageMagnet[Int] {
    def apply(): Int = {
      // logic from handling a P2PResponse
      println("Handling P2P future response")
      8008
    }
  }
  import scala.concurrent.ExecutionContext.Implicits.global
  receive(Future(new P2PRequest))
  receive(Future(new P2PResponse))

  // 2. lifting works -- with a catch
  trait MathLib {
    def add1(x: Int): Int = x + 1
    def add1(s: String): Int = s.toInt + 1
    // other add1 overloads
  }
  // "magnetize"
  trait AddMagnet { // notice: no type parameter... compiler can't handle this trait has a type parameter
    def apply(): Int
  }
  def add1(magnet: AddMagnet) = magnet()

  implicit class AddInt(x: Int) extends AddMagnet {
    def apply(): Int = x + 1
  }
  implicit class AddString(x: String) extends AddMagnet {
    def apply(): Int = x.toInt + 1
  }
  val add1FV = add1 _
  println(add1FV(18) + add1FV("22")) // 42 -- nice!

  // problem:
  // val receiveFV = receive _
  // receiveFV: MagnetPattern.MessageMagnet[Nothing] => Nothing

  /*
    Drawbacks of magnet pattern:
    1. verbose -- extra trait, implicit classes, etc.
    2. api is harder to read
    3. can't assign default arguments
    4. call by name doesn't work properly (exercise: prove it)
   */

  class Handler {
    def handle(s: => String) = {
      println(s)
    }
  }

  trait HandleMagnet {
    def apply(): String
  }

  def handle(magnet: => HandleMagnet) = magnet()

  implicit class StringHandle(s: String) extends HandleMagnet {
    def apply() = {
      println("message")
      s
    }
  }

  // only prints "message" once
  println("----- implicitly called -----")
  handle("abc")
  // prints "message" twice
  println("----- explicitly called -----")
  handle {
    println("message")
    "abc"
  }
}
