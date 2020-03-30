package lectures.extra

object CallByNameVsCallByValue extends App {

  def simple: Int = {
    println("Simple function called")
    42
  }

  def callByNameFn(x: => Int): Unit = {
    println("x1= " + x)
    println("x2= " + x)
    println("x3= " + x)
  }

  def callByValueFn(x: Int): Unit = {
    println("x1= " + x)
    println("x2= " + x)
    println("x3= " + x)
  }

  println("----- Call by name -----")
  callByNameFn(simple)
  println("----- Call by value -----")
  callByValueFn(simple)

  /*
    In the call-by-value version, the side-effect of the passed-in function call (something()) only happened once.
    However, in the call-by-name version, the side-effect happened twice.

    This is because call-by-value functions compute the passed-in expression's value before calling the function,
    thus the same value is accessed every time.
    Instead, call-by-name functions recompute the passed-in expression's value every time it is accessed.
   */
}
