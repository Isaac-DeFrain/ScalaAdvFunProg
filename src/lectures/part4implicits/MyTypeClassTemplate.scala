package lectures.part4implicits

trait MyTypeClassTemplate[T] {
  def method(value: T): String // all instances of this type class must implement these methods
  def anotherMethod(v1: T, v2: T): T
}
// companion object which surfaces the implicit instance of the type class
// gives access to the entire type class interface, i.e. all the methods
object MyTypeClassTemplate {
  def apply[T](implicit instance: MyTypeClassTemplate[T]) = instance
}