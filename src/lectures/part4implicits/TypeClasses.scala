package lectures.part4implicits

import exercises.EqualityPlayground.UserEqual

object TypeClasses extends App {

  // A *type class* is a trait that takes a type and describes what methods can be applied to that type.

  trait HTMLWritable {
    def toHTML: String
  }

  // Option 1: shitty single implementation
  case class User(name: String, age: Int, email: String) extends HTMLWritable {
    override def toHTML: String = s"<div> $name ($age yo) <a href=$email/> </div>"
  }

  User("Quantifier", 666, "quantifiertech@gmail.com").toHTML
  /*
    Disadvantages:
      1. only works for the types WE write -- we would need to write conversion for standard types
      2. only ONE implementation
   */

  // Option 2: pattern matching
  object HTMLSerializerPM {
    def serializeToHtml(value: Any) = value match {
      case User(n, a, e)  =>
      //case java.util.Date =>
      case _ =>
    }
  }
  /*
    Disadvantages:
      1. lost type safety because serializeToHtml accepts Any
      2. need to modify code every time
      3. still ONE implementation
   */

  // Option 3: type classes
  trait HTMLSerializer[T] {
    def serialize(value: T): String
  }

  implicit object UserSerializer extends HTMLSerializer[User] {
    def serialize(user: User): String = s"<div> ${user.name} (${user.age} yo) <a href=${user.email}/> </div>"
  }

  val quantifier = User("Quantifier", 666, "quantifiertech@gmail.com")
  println(UserSerializer.serialize(quantifier)) // <div> Quantifier (666 yo) <a href=quantifiertech@gmail.com/> </div>

  val isaac = User("Isaac", 666, "someAddress@gmail.com")
  println(UserEqual(isaac, quantifier)) // false

  /*
    Advantages of this design:
      1. we can define serializers for different types
      2. we can define MULTIPLE implementations for a given type (without losing type safety)
   */

  import java.util.Date
  object DateSerializer extends HTMLSerializer[Date] {
    override def serialize(date: Date): String = s"<div> ${date.toString()} </div>"
  }

  object PartialUserSerializer extends HTMLSerializer[User] {
    override def serialize(user: User): String = s"<div> ${user.name} </div>"
  }

  // part 2 -- implicit type class instances
  implicit object HTMLSerializer {
    def serialize[T](value: T)(implicit serializer: HTMLSerializer[T]): String =
      serializer.serialize(value)

    def apply[T](implicit serializer: HTMLSerializer[T]) = serializer
  }

  implicit object IntSerializer extends HTMLSerializer[Int] {
    override def serialize(value: Int): String = s"<div style:color=blue> $value </div>"
  }

  println(HTMLSerializer.serialize(42)) // <div style:color=blue> 42 </div>

  // part 3
  implicit class HTMLEnrichment[T](value: T) {
    def toHTML(implicit serializer: HTMLSerializer[T]): String = serializer.serialize(value)
  }

  // can pass in whatever serializer we want
  println(isaac.toHTML) // <div> Isaac (666 yo) <a href=someAddress@gmail.com/> </div>

  /*
    - extends functionality to new types!
    - allows for different implementations for the same type!
    - super expressive
   */

  println(2.toHTML)
  // println(isaac.toHTML(PartialUserSerializer)) -- expects type: Int ??? WTF!?

  /*
    Type class pattern:
      - type class itself -- HTMLSerializer[T] { .. methods we want to expose .. }
      - type class instances (some of which are implicit) -- UserSerializer, IntSerializer
      - conversion with implicit classes (allow the use type class instances as implicit parameters) -- HTMLEnrichment[T]
   */

  // context bounds
  // big signature
  def htmlBoilerplate[T](content: T)(implicit serializer: HTMLSerializer[T]): String = {
    s"<html><body> ${content.toHTML(serializer)} </body></html>"
  }

  // compact, but cannot use serializer by name...
  def htmlSugar[T : HTMLSerializer](content: T): String = {
    val serializer = implicitly[HTMLSerializer[T]]
    // use serializer explicitly
    s"<html><body> ${content.toHTML(serializer)} </body></html>"
  }
  println(htmlSugar(isaac)) // <html><body> <div> Isaac (666 yo) <a href=someAddress@gmail.com/> </div> </body></html>

  // implicitly
  case class Permissions(mask: String)
  implicit val defaultPermissions: Permissions = Permissions("666")

  // in some other part of the code, we want to surface out the implicit parameter
  val stdPerms = implicitly[Permissions]
  println(stdPerms) // Permissions(666)

  // how could one implement an implicitly method???

  /*
    - type class = trait with type parameter -- trait TypeClass[T] {...}
    - type class instances are often implicit i.e. implicit object IntTypeClassInstance extends TypeClass[Int] {...}
    - invoke type class instance i.e. object TypeClass { def apply(implicit instance: TypeClass[T]) = instance }
    - enriching types with type classes (pimp my library style shit)
      i.e. implicit class ConversionClass[T](value: T) { def action(implicit instance: TypeClass[T]) = instance.action(value) }
   */
}
