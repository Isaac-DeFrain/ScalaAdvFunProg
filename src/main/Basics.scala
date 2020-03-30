package main

object Basics extends App {

  trait Animal {
    val food: String
    def eat(): Unit
  }

  class Dog(val name: String) extends Animal {
    val food = "Dog food"
    def eat() = println(s"$name eats yummy food!")
  }

  val dogName = new Dog("Fido").name
  println(dogName)

  val dogFood = new Dog("Kujo").eat

}
