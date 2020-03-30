package lectures.part2afp

object FunctionalCollections extends App {

  // Sets are callable, i.e. they have an apply method.
  val aSet = Set(1,2,3)
  println(aSet(2)) // true
  println(aSet(5)) // false
  println(aSet(3) == aSet.contains(3)) // true <=> equivalent

  // Sets ARE functions!

  // Remainder of lesson in exercises.MySet
}
