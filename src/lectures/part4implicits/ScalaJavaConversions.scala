package lectures.part4implicits

import java.{util => ju}

object ScalaJavaConversions extends App {

  import collection.JavaConverters._

  val javaSet: ju.Set[Int] = new ju.HashSet[Int]()
  (1 to 5).foreach(javaSet.add)
  println("----- Java Set -----")
  println(javaSet)

  val scalaSet = javaSet.asScala
  println("----- Scala Set -----")
  println(scalaSet)

  /*
    Iterator
    Iterable
    ju.List - scala.mutable.Buffer
    ju.Set  - scala.mutable.Set
    ju.Map  - scala.mutable.Map
   */

  import collection.mutable._
  val numbersBuffer = ArrayBuffer[Int](1,2,3,4,5)
  val juNumbersBuffer = numbersBuffer.asJava
  println("----- Scala Array Buffer -----")
  println(numbersBuffer)
  println("----- Java Array Buffer -----")
  println(juNumbersBuffer)
  println("----- Mutable collections -----")
  println("-- references are the same")
  println(juNumbersBuffer.asScala eq numbersBuffer) // true -- same references (mutable) -- shallow eq
  println("-- collections are the same")
  println(juNumbersBuffer.asScala == numbersBuffer) // true -- same collections

  // immutable doesn't work the same way
  val numList = List(1,2,3,4,5)
  val juNumList = numList.asJava
  println("----- Immutable collections -----")
  println("-- references are different")
  println(juNumList.asScala eq numList) // false -- different instances
  println("-- collections are the same")
  println(juNumList.asScala == numList) // true  -- same collections

  /*
    Exercise: create a Scala-Java Optional-Option
        .asScala
   */
}
