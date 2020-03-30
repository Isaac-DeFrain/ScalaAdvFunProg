package lectures.part2afp

object CurryAndPAFs extends App {

  // Curried functions
  val superAdder: Int => Int => Int =
    x => y => x + y

  val add3 = superAdder(3)
  println(add3)
  println(add3(5))

  // METHOD!
  /**
   * Methods are part of instances of classes.
   * A partially applied curried method must be lifted to function status.
   * Limitation of JVM: methods != functions
   */
  def curriedAdder(x: Int)(y: Int) = x + y // curried method

  val add4: Int => Int = curriedAdder(4) // alternative: no type annotation, curriedAdder(4) _ (does eta-expansion
  println(add4(5))

  // Lifting = Eta-expansion
  def inc(x: Int) = x +1
  val incList = List(1,2,3).map(inc) // compiler does eta-expansion, i.e. rewrites as List(1,2,3).map(x => inc(x))
  println(incList)

  // Force compiler to do eta-expansion when we want
  // Partial function application
  val add5 = curriedAdder(5) _ // Int => Int

  // Exercise
  val simpleAddFunction = (x: Int, y: Int) => x + y
  def simpleAddMethod(x: Int, y: Int) = x + y
  def curriedAddMethod(x: Int)(y: Int) = x + y

  // add7: Int => Int = y => 7 + y
  val add7_1 = curriedAddMethod(7) _
  val add7_2 = (x: Int) => simpleAddFunction(7, x)
  val add7_3 = (x: Int) => simpleAddMethod(7, x)
  val curryAdd2 = curriedAddMethod(2) _
  val curryAdd5 = curriedAddMethod(5) _
  val add7_4 = (x: Int) => curryAdd2(curryAdd5(x))
  val add7_5 = (x: Int) => curryAdd5(curryAdd2(x))

  // Daniel's solutions:
  val add7_6 = simpleAddFunction.curried(7)
  val add7_7 = simpleAddMethod(7, _: Int) // alternative syntax for turning methods into function values
  val add7_8 = simpleAddFunction(7, _: Int)

  // Underscores are powerful!
  def concatenator(a: String, b: String, c: String) = a + b + c
  // (x: String) => concatenator("Hello, I'm ", x, ". How are you doing?")
  val simpleInsert = concatenator("Hello, I'm ", _: String, ". How are you doing?")
  println(simpleInsert("Isaac"))

  // (x: String, y: String) => concatenator("Hello, ", x, y)
  val fillInTheBlanks = concatenator("Hello, ", _: String, _: String)
  println(fillInTheBlanks("Bob", " Blob."))

  // Exercises
  /*
    1. Process a list of numbers and return their string representation with different formats.
       Use %4.2f, %8.6f, and %14.12f with a curried formatter function.

    2. Differences between:
       - functions vs methods
       - parameters: by name vs 0-lambdas
   */
  // 1.
  def curriedFormatter(x: String)(y: Double) = x.format(y)
  println("----- A bunch of differently formatted numbers -----")
  List(1.0,2.0,3.1,4.2,5.0).foreach(x => println(curriedFormatter("%4.2f")(x)))
  List(1.0,2.0,3.1,4.2,5.0).foreach(x => println(curriedFormatter("%8.6f")(x)))
  List(1.0,2.0,3.1,4.2,5.0).foreach(x => println(curriedFormatter("%14.12f")(x)))

  // 2.
  def byName(n: => Int) = n + 1          // implementation doesn't matter
  def byFunction(f: () => Int) = f() + 1 // implementation doesn't matter

  def method: Int = 41
  def parenMethod(): Int = 41

  println("----- byName -----")
  val byName_int = byName(41)
  println(byName_int)
  val byName_meth = byName(method)
  println(byName_meth)
  val byName_pMeth = byName(parenMethod())
  println(byName_pMeth)
  val byName_lambda = byName(((x: Int) => x)(41))
  println(byName_lambda)
  val byName_paf = (byName(_))(41)
  println(byName_paf)

  // A function value cannot be used as a by name parameter

  println("----- byFunction -----")
  val byFun_int = byFunction(() => 41)
  println(byFun_int)
  val byFun_meth = byFunction(() => method) // byFunction(method) -- not ok, compiler does not do eta-expansion
  println(byFun_meth)
  val byFun_pMeth = byFunction(parenMethod) // compiler does eta-expansion -- difference between accessor methods and proper methods!
  println(byFun_pMeth)
  val byFun_lambda = byFunction(() => 41)
  println(byFun_lambda)
  val byFun_paf = byFunction(parenMethod _) // the _ is not necessary here
  println(byFun_paf)
}
