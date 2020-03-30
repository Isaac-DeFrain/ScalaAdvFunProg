package lectures.part4implicits

object ImplicitsIntro extends App {

  val strPair = "Isaac" -> "666"
  val intPair = 42 -> 42

  case class Person(name: String) {
    def greet = s"Hi, my name is $name"
  }

  implicit def stringToPerson(str: String): Person = Person(str)

  println("Isaac".greet) // compiler rewrites to println(stringToPerson("Isaac").greet
                         // since it's given a string and greet is a method defined on Person,
                         // it searches for and finds the method with signature: String => Person, i.e. stringToPerson

  // compiler no like-y this
//  class A {
//    def greet = "I'm A"
//  }
//  implicit def stringToA(str: String): A = new A

  // implicit parameters
  def increment(x: Int)(implicit amt: Int) = x + amt
  implicit val defaultAmt = 41

  println(increment(1)) // 42
  // NOT default args

  // Futures were constructed with an implicit parameter (execution context)
  // From Future.class:
  // def apply[T](body : => T)(implicit executor : scala.concurrent.ExecutionContext) : scala.concurrent.Future[T]
  // and from ExecutionContext.class:
  // implicit lazy val global : scala.concurrent.ExecutionContext
}
