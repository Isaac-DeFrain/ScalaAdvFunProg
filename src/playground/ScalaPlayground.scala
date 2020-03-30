package playground

object ScalaPlayground extends App {

  println("Hello, Scala")

  // Decomposing sequences - pattern matching on user-defined set implementation
  abstract class MySet[+A] {
    def elem: A = ???
    def rest: MySet[A] = ???
    //def contains(elem: A): Boolean = ??? // covariant type A occurs in contravariant position in type A of value elem
  }
  case object EmptySet extends MySet[Nothing]
  case class Union[+A](override val elem: A, override val rest: MySet[A]) extends MySet[A]

  object MySet {
    def unapplySeq[A](set: MySet[A]): Option[Seq[A]] = {
      if (set == EmptySet) Some(Seq.empty)
      else unapplySeq(set.rest).map(set.elem +: _)
    }
  }
  val aSet: MySet[String] = Union("a", Union("b", Union("c", EmptySet)))
  println(aSet match {
    case MySet("a","b",_*) => "success"
    case _                 => "failure"
  })
  // This set implementation is not commutative...
  println(aSet match {
    case MySet("b","a",_*) => "success"
    case _                 => "failure"
  })
}
