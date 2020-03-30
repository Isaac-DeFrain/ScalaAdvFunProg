package lectures.part2afp

object PartialFunctions extends App {

  // function, i.e. total function
  val aFunction = (x: Int) => x + 1 // Function1[Int, Int] === Int => Int

  // slightly forced & clunky "partial" function
  val aFussyFunction = (x: Int) =>
    if (x == 1) 42
    else if (x == 2) 55
    else if (x == 7) 666
    else throw new FunctionApplicableException
  class FunctionApplicableException extends RuntimeException

  // will throw MatchException if x =/= 1,2,7
  val aNicerFussyFunction = (x: Int) => x match {
    case 1 => 42
    case 2 => 55
    case 7 => 666
  }
  // {1,2,7} => Int

  val aPartialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 55
    case 7 => 666
  } // partial function value
  println(aPartialFunction(7)) // 666
  // println(aPartialFunction(12)) crashes with a MatchError because partial functions are based on pattern matching

  println(aNicerFussyFunction == aPartialFunction) // false

  // Partial function utilities
  println(aPartialFunction.isDefinedAt(12)) // false

  // partial functions can be lifted to total functions returning Option
  val lifted = aPartialFunction.lift // Int => Option[Int]
  println(lifted(7))  // Some(666)
  println(lifted(12)) // None

  val pfChain = aPartialFunction.orElse[Int, Int] {
    case 67 => 12
  }
  println(pfChain(2))  // 55
  println(pfChain(67)) // 12

  // PF extend normal functions
  val aTotalFunction: Int => Int = {
    case 12 => 42
  } // partial functions are a subtype of total functions

  // HOFs accept partial functions as well
  val aMappedList = List(1,2,3).map {
    case 1 => 12
    case 2 => 42
    case 3 => 666
  }
  println(aMappedList) // List(12,42,666)

  /*
  Note: Unlike functions which can have multiple parameter types, partial functions can only have ONE parameter type.
   */

  /**
   * Exercises:
   * 1. Construct PF instance
   * 2. dumb chatbot as PF
   */

  // Reading from console.
  //scala.io.Source.stdin.getLines().foreach(line => println("You said: " + line))

  // first attempt:
  val partial1: PartialFunction[Int, String] = new PartialFunction[Int, String] {
    override def apply(x: Int): String = {
      if (x == 1) "success"
      else "failure"
    }
    override def isDefinedAt(x: Int): Boolean = x match {
      case 1 => true
      case _ => false
    }
    override def orElse[A1 <: Int, B1 >: String](that: PartialFunction[A1, B1]): PartialFunction[A1, B1] = super.orElse(that)
    override def andThen[C](k: String => C): PartialFunction[Int, C] = super.andThen(k)
    override def lift: Int => Option[String] = super.lift
    override def applyOrElse[A1 <: Int, B1 >: String](x: A1, default: A1 => B1): B1 = super.applyOrElse(x, default)
    override def runWith[U](action: String => U): Int => Boolean = super.runWith(action)
  }
  println(partial1(1)) // "success"
  println(partial1.isDefinedAt(1)) // true
  println(partial1(2)) // "failure"
  println(partial1.isDefinedAt(2)) // false

  // dumb chatbot
  val dumbChatBot: PartialFunction[String, String] = {
    case "Hi" => "Whazzzzup!?!?! What's your name bro?"
    case _    => "Unidentified object. Please try again."
  }
  scala.io.Source.stdin.getLines().foreach(line => println(dumbChatBot(line)))

  // Daniel's solution
  val partial2: PartialFunction[Int, String] = new PartialFunction[Int, String] {
    override def apply(x: Int): String = x match {
      case 1 => "success"
      case 2 => "success"
      case 3 => "failure"
    }
    override def isDefinedAt(x: Int): Boolean = {
      x == 1 || x == 2 || x == 3
    }
  }
  println(partial2.isDefinedAt(5)) // false

  scala.io.Source.stdin.getLines().map(dumbChatBot).foreach(println)
}
