package lectures.part4implicits

object PimpMyLibrary extends App {

  // enriching a library with implicits

  // Want: 2.isPrime

  // implicit classes take one and only one arg
  implicit class RichInt(value: Int) {
    def isEven: Boolean = value % 2 == 0
    def sqrt = Math.sqrt(value)
    def times(f: () => Unit) = {
      def timesAux(n: Int): Unit =
      if (n < 1) ()
      else {
        f()
        timesAux(n - 1)
      }
      timesAux(value)
    }
    def *[T](list: List[T]): List[T] =
      if (value < 1) List.empty
      else (value - 1) * list ++ list
  }
  println(42.times(() => println("dope!")))
  println(19 * List(23))

  RichInt(42)
  42.sqrt

  // type enrichment = pimping

  import scala.concurrent.duration._

  3.seconds

  // compiler doesn't do multiple implicit searches
  // e.g.
  //implicit class RicherInt(richInt: RichInt) {
  //  def isOdd = richInt.value % 2 != 0
  //}
  //42.isOdd

  /*
    Exercises:
    - enrich the String class
      - asInt
      - encrypt

    - further enrich the Int class
      - times(function)
      - *(List)
   */

  implicit class RichString(str: String) {
    def asInt = str.toInt
    def encrypt(param: Int) = str.map(c => (c.toInt + param).toChar)
  }
  println("secret message".encrypt(13))

  // equivalent: implicit class RichAltInt(value: Int)
  class RichAltInt(value: Int) {
    implicit def enrich(value: Int): RichAltInt = new RichAltInt(value)
  }

  // **Danger zone**
  // bugs in implicit defs are VERY hard to track down -- avoid them at all costs!!!
  // e.g.
  implicit def intToBoolean(int: Int): Boolean = int == 1
  val aCondVal = if (3) "Great!" else "Terrible!"
  println(aCondVal)

  /*
    Tips:
    - keep enrichment to implicit classes and type classes
    - avoid implicit defs if possible
    - package implicits clearly and bring into scope only what you need when you need it
    - if type conversions are needed, make them SPECIFIC
   */
}
