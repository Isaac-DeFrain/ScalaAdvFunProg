package lectures.part3concurrency

import java.util.concurrent.Executors

object Intro extends App {

  /*
  In java.lang, there is
    interface Runnable {
      public void run()
    }
   */
  // JVM threads
  val runnable = new Runnable {
    override def run(): Unit = println("Running")
  }
  val aThread1 = new Thread(new Runnable {
    override def run(): Unit = println("Running 1")
  })
  val aThread2 = new Thread(new Runnable {
    override def run(): Unit = println("Running 2")
  })

  // Thread creation
  // creates a JVM thread => runs on top of an OS thread
  //  aThread1.start() // gives the signal to the JVM to start a JVM thread
  //  aThread2.start()
  runnable.run() // doesn't do anything in parallel

  // Thread joins
  aThread1.join() // blocks until aThread1 finishes computation

  val helloThread = new Thread(() => (1 to 5).foreach(_ => println("Hello")))
  val goodbyeThread = new Thread(() => (1 to 5).foreach(_ => println("Goodbye")))
  //  helloThread.start()
  //  goodbyeThread.start()
  // different runs produce different results
  // thread scheduling depends on several factors
  // threads are expensive to start and kill

  // executors
  val pool = Executors.newFixedThreadPool(10)
  //  pool.execute(() => println("Something in the thread pool"))
  //
  //  pool.execute(() => {
  //    Thread.sleep(1000)
  //    println("Done after 1 sec")
  //  })
  //
  //  pool.execute(() => {
  //    Thread.sleep(1000)
  //    println("Almost done")
  //    Thread.sleep(1000)
  //    println("Done after 2 sec")
  //  })

  // shutdown thread pool
  //pool.shutdown()
  //pool.execute(() => println("Should not print")) // throws exception in calling thread, other threads finish computing

  //pool.shutdownNow() // interrupts concurrently running threads and throws exception

  //  pool.shutdown()
  //  println(if (pool.isShutdown) "Pool is shutdown" else "Still accepting jobs") // Prints Pool is shutdown before threads finish executing

  // Concurrency Problems on the JVM
  def runInParallel = {
    var x = 0

    val thread1 = new Thread(() => {
      x = 1
    })

    val thread2 = new Thread(() => {
      x = 2
    })

  //    thread1.start()
  //    thread2.start()
  //    println(x)
  }

  //for (_ <- 1 to 10000) runInParallel // rac condition

  class BankAcct(var amount: Int) {
    override def toString: String = amount.toString
  }

  def buy(account: BankAcct, thing: String, price: Int) = {
    account.amount -= price
    println("I bought a " + thing)
    println("The account is: " + account)
  }

  //  for (_ <- 1 to 100) {
  //    val acct = new BankAcct(1000)
  //    val threadOstrich = new Thread(() => buy(acct, "Ostrich", 500))
  //    val threadCrystal = new Thread(() => buy(acct, "crystal", 100))
  //    val threadBlockchain = new Thread(() => buy(acct, "blockchain", 350))
  //    threadOstrich.start()
  //    threadCrystal.start()
  //    threadBlockchain.start()
  //    Thread.sleep(10)
  //    println()
  //    if (acct.amount != 50) println("WHAT!?!? Account has balance: " + acct.amount)
  //  }

  // Option #1: use synchronized()
  def buySafe(account: BankAcct, thing: String, price: Int) = {
    account.synchronized {
      // no two threads can evaluate this block at the same time
      account.amount -= price
      println("I bought a " + thing)
      println("The account is: " + account)
    }
  }

  //  for (_ <- 1 to 100) {
  //    val acct = new BankAcct(1000)
  //    val threadOstrich = new Thread(() => buySafe(acct, "Ostrich", 500))
  //    val threadCrystal = new Thread(() => buySafe(acct, "crystal", 100))
  //    val threadBlockchain = new Thread(() => buySafe(acct, "blockchain", 350))
  //    threadOstrich.start()
  //    threadCrystal.start()
  //    threadBlockchain.start()
  //    Thread.sleep(10)
  //    println()
  //    if (acct.amount != 50) println("WHAT!?!? Account has balance: " + acct.amount)
  //  }

  // Option #2: use @volatile annotation on var

  /**
   * EXERCISES
   *
   * 1) Construct 50 "inception" threads: thread1 -> thread2 -> ...
   *    println("Hello from thread #")
   *    print in reverse order
   */

  // 1) Inception threads
  def inceptionThreads(max: Int, i: Int = 1): Thread = new Thread(() => {
    if (i < max) {
      val newThread = inceptionThreads(max, i + 1)
      newThread.start()
      newThread.join()
    }
    println(s"Hello from thread $i")
  })

  // inceptionThreads(50).start()

  // 2) What is largest value? 100
  //    smallest value? 1
  //  var x = 0
  //  val threads = (1 to 100) map {_ => new Thread(() => x += 1) }
  //  threads.foreach(_.start())
  //  threads.foreach(_.join())
  //  println(x)

  // 3) Sleep fallacy
  var message = ""
  val awesomeThread = new Thread(() => {
    Thread.sleep(1000)
    message = "Scala is awesome!"
  })

  message = "Scala sucks!"
  awesomeThread.start()
  Thread.sleep(1001)
  awesomeThread.join() // wait for the awesome thread to join
  println(message)

  /*
    What's the value of the message? almost always "Scala is awesome!"
    NOT guaranteed though

    (main thread)
      message = "Scala sucks!"
      awesomeThread.start()
      sleep() - relieves execution
    (awesomeThread)
      sleep() - relieves execution
    (OS gives the CPU to some important thread - takes CPU more than 2 seconds)
    (OS gives the CPU back to the MAIN thread)
      println("Scala sucks!")
    (OS gives the CPU to awesomeThread)
      message = "Scala is awesome!"
   */
}
