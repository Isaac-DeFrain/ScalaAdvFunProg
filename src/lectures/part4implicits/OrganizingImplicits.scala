package lectures.part4implicits

object OrganizingImplicits extends App {

  implicit val reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _) // takes precedence over Int ordering in scala.Predef
  // implicit def reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _) -- works same as above
  // implicit def reverseOrdering(): Ordering[Int] = Ordering.fromLessThan(_ > _) -- uses scala.Predef ordering instead
  println(List(4,1,2,5,3).sorted) // implicit ordering for Int invoked by sorted method

  /*
    Implicits (used as implicit parameters):
      - val/var
      - object
      - accessor methods == defs with no parentheses
      - above can only be defined within an object, class, or trait; cannot be defined as top-level
   */

  case class Person(name: String, age: Int)

  val persons = List(
    Person("Chidi", 42),
    Person("Elenor", 43),
    Person("Jason", 29),
    Person("Tahani", 35)
  )
  implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.age < b.age)
  //implicit val nameOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)

  // Does not compile!
  // object SomeObject {
  //   implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.age < b.age)
  // }

  // Does compile!
  // object Person {
  //   implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.age < b.age)
  // }

  //println(persons.sorted)

  /*
    Implicit scope:
      - normal scope == LOCAL scope
      - imported scope
      - companions of all types involved in the method signature
        - List
        - Ordering
        - all the types involved == A or supertypes
   */
  // def sorted[B >: A](implicit ord : scala.math.Ordering[B]) : List[B]

  /*
    Best practices:
      When defining an implicit:
      1. There is a *single possible* value for it
         and you can edit the code for the type
      Then define an implicit in the companion.

      2. If there are *many possible values* for it
         but a *single* good one
         and you can edit the code for the type
      Then define the *good* implicit in the companion (others in local scope or other objects.

      3. If there are *many good possible values* for it
      Then define each in it's own companion object and make the user import the right container.
   */

  object AgeOrdering {
    implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.age < b.age)
  }
  object NameOrdering {
    implicit val nameOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  // Age sorted list
  import AgeOrdering._ // or AgeOrdering.ageOrdering to be more specific
  println(persons.sorted)

  // Name sorted list
  import NameOrdering._
  println(persons.sorted)

  /*
    Exercise:
      - totalPrice = most used (50%)
      - unit count (25%)
      - unit price (25%)
   */
  case class Purchase(nUnits: Int, unitPrice: Double)

  // to be used by default
  object Purchase {
    implicit val totalPriceOrdering: Ordering[Purchase] = {
      def totalPrice(p: Purchase) = p.nUnits * p.unitPrice
      Ordering.fromLessThan(totalPrice(_) < totalPrice(_))
    }
  }
  object Count {
    implicit val countOrdering: Ordering[Purchase] = Ordering.fromLessThan(_.nUnits < _.nUnits)
  }
  object UnitPrice {
    implicit val unitPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan(_.unitPrice < _.unitPrice)
  }

}
