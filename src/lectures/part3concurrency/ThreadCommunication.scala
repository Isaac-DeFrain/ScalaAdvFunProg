package lectures.part3concurrency

import scala.collection.mutable
import scala.util.Random

object ThreadCommunication extends App {

  /* Producer-consumer problem

      producer -> [x] -> consumer
   */

  class SimpleContainer {
    private var value: Int = 0

    def isEmpty: Boolean = value == 0
    def set(newValue: Int) = value = newValue
    def get: Int = {
      val result = value
      value = 0
      result
    }
  }

  // Naive producer-consumer
  def naiveProdCons(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting")
      while(container.isEmpty) {
        println("[consumer] actively waiting")
      }
      println("[consumer] I have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(500)
      val value = 42
      println("[producer] I have produced " + value)
      container.set(value)
    })

    producer.start()
    consumer.start()
  }

  //naiveProdCons() // very naive -- lots of busy waiting...

  // Wait and Notify
  /*
   Revisiting synchronized

   Entering a synchronized expression on an object *locks the object*:
     val someObj = "hello"
     someObj.synchronized { -- locks the object's *monitor* -- tracks which object is locked by which thread
       // code              -- any other thread trying to run this will block -- until evaluation is finished
     }                      -- release the lock -- any other thread is free to evaluate the expression

   Only AnyRefs can have synchronized blocks (primitive types like Int, Boolean, etc cannot)

   General principles:
     - Make no assumptions about who gets the lock first, there are no guarantees
     - Keep locking to a minimum for performance reasons
     - Maintain *thread safety* at ALL TIMES in parallel applications (more important than performance)

   wait() and notify()
     wait()-ing on an object's monitor suspends the calling thread indefinitely

     // thread 1
     val someObj = "hello"
     someObj.synchronized { -- locks the object's monitor
       // code part 1
       someObj.wait()       -- releases the lock and waits...
       // code part 2       -- when allowed to proceed, the monitor is locked again and this code executes
     }

     // thread 2
     someObj.synchronized { -- locks the object's monitor
       // ...code
       someObj.notify()     -- signals ONE thread waiting on the object's monitor to continue, no guarantees on which thread (up to JVM and OS)
                            -- to signal all threads, use notifyAll()
       // ...more code
     }                      -- but only after the rest of the code finishes executing and the object's monitor is unlocked

   Waiting and notifying only work in *synchronized* expressions
   */

  // Smarter producer-consumer
  def smartProdCons(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting patiently...")
      container.synchronized {
        container.wait()

        // container must have some value
        println("[consumer] I have consumed the value " + container.get)
      }
    })

    val producer = new Thread(() => {
      println("[producer] working...")
      container.synchronized {
        Thread.sleep(2000)
        val newValue = 42
        println("[producer] I have produced the value " + newValue)
        container.set(newValue)
        container.notify()
      }
    })

    consumer.start()
    producer.start()
  }

  //smartProdCons()

  /*
  Buffer with multiple values
    producer -> [ ? ? ? ] -> consumer

    If buffer is full, producer must block until values are consumed
    If buffer is empty, consumer must block until values are produced
   */

  def bufferProdCons(): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 3

    val consumer = new Thread(() => {
      val random = new Random()

      while (true) {
        buffer.synchronized {
          if (buffer.isEmpty) {
            println("[consumer] queue is empty, waiting...")
            buffer.wait()
          }
          // there must be at least ONE value in the queue
          val value = buffer.dequeue()
          println("[consumer] I consumed the value " + value)
          buffer.notify()
        }
        Thread.sleep(random.nextInt(500))
      }
    })

    val producer = new Thread(() => {
      val random = new Random()
      var i = 0

      while (true) {
        buffer.synchronized {
          if (buffer.size == capacity) {
            println("[producer] Buffer is full, waiting...")
            buffer.wait()
          }
          // there must be at least ONE empty spot in the queue
          println("[producer] I'm producing the value " + i)
          buffer.enqueue(i)
          buffer.notify()
          i += 1
        }
        Thread.sleep(random.nextInt(500))
      }
    })

    consumer.start()
    producer.start()
  }

  // bufferProdCons()

  /*
    Multiple producers and consumers on the same buffer
   */

  def multipleProdCons(number: Int, capacity: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]

    def singleProdCon(n: Int): Unit = {
      val consumer = new Thread(() => {
        val random = new Random()

        while (true) {
          buffer.synchronized {
            while (buffer.isEmpty) {
              println(s"[consumer $n] queue is empty, waiting...")
              buffer.wait()
            }
            // there must be at least ONE value in the queue
            val value = buffer.dequeue()
            println(s"[consumer $n] I consumed the value " + value)
            buffer.notify()
          }
          Thread.sleep(random.nextInt(500))
        }
      })

      val producer = new Thread(() => {
        val random = new Random()
        var i = 0

        while (true) {
          buffer.synchronized {
            while (buffer.size == capacity) {
              println(s"[producer $n] Buffer is full, waiting...")
              buffer.wait()
            }
            // there must be at least ONE empty spot in the queue
            println(s"[producer $n] I'm producing the value " + i)
            buffer.enqueue(i)
            buffer.notify()
            i += 1
          }
          Thread.sleep(random.nextInt(400))
        }
      })
      consumer.start()
      producer.start()
    }

    if (number <= 0) ()
    else
      singleProdCon(number)
      multipleProdCons(number - 1, capacity)
  }

  // multipleProdCons(4, 5)

  // using classes

  class Consumer(id: Int, buffer: mutable.Queue[Int], sleepTime: Int) extends Thread {
    override def run(): Unit = {
      val random = new Random()

      while (true) {
        buffer.synchronized {
          while (buffer.isEmpty) {
            println(s"[consumer $id] queue is empty, waiting...")
            buffer.wait()
          }
          // there must be at least ONE value in the queue
          val value = buffer.dequeue()
          println(s"[consumer $id] I consumed the value " + value)
          buffer.notify()
        }
        Thread.sleep(random.nextInt(sleepTime))
      }
    }
  }

  class Producer(id: Int, buffer: mutable.Queue[Int], sleepTime: Int, capacity: Int) extends Thread {
    override def run(): Unit = {
      val random = new Random()
      var i = 0

      while (true) {
        buffer.synchronized {
          while (buffer.size == capacity) {
            println(s"[producer $id] Buffer is full, waiting...")
            buffer.wait()
          }
          // there must be at least ONE empty spot in the queue
          println(s"[producer $id] I'm producing the value " + i)
          buffer.enqueue(i)
          buffer.notify()
          i += 1
        }
        Thread.sleep(random.nextInt(sleepTime))
      }
    }
  }

  def multipleProdConsFromClasses(nProd: Int, nCons: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 5
    (1 to nProd).foreach(i => new Producer(i, buffer, 400, capacity).start())
    (1 to nCons).foreach(i => new Consumer(i, buffer, 500).start())
  }

  // multipleProdConsFromClasses(5, 5)

  /*
    Examples:
    1. notifyAll acts differently than notify
    2. deadlock
    3. livelock
   */

  // 1. notifyAll()
  def testNotifyAll(): Unit = {
    val bell = new Object

    (1 to 10).foreach(i => new Thread(() => {
      bell.synchronized {
        println(s"[child $i] sleeping...")
        bell.wait()
        println(s"[child $i] Up and at 'em!")
        // children don't notify
      }
    }).start())

    new Thread(() => {
      Thread.sleep(2000)
      println("[mean parent] Wake up you maggots!!!")
      bell.synchronized {
        bell.notify()
      }
    }).start()
  }

  // testNotifyAll()

  // 2. deadlock - Bow down
  case class Friend(name: String) {
    def bow(other: Friend): Unit = {
      this.synchronized {
        println(this.name + ": I am bowing to my friend, " + other.name)
        other.rise(this)
        println(this.name + ": my friend, " + other.name + ", has risen")
      }
    }
    def rise(other: Friend): Unit = {
      this.synchronized {
        println(this.name + ": I am rising to my friend, " + other.name)
      }
    }

    var side = "right"
    def switchSide(): Unit = {
      if (side == "right") side = "left" else side = "right"
    }
    def pass(other: Friend): Unit = {
      while (this.side == other.side) {
        println(this.name + ": Oh, please, " + other.name + ", after you.")
        switchSide()
        Thread.sleep(1000)
      }
    }
  }

  val alice = Friend("Alice")
  val bob = Friend("Bob")

  //new Thread(() => alice.bow(bob)).start() // alice's lock |   bob's lock
  //new Thread(() => bob.bow(alice)).start() //   bob's lock | alice's lock
  // similar to dining philosopher's problem

  // 3. livelock
  new Thread(() => alice.pass(bob)).start() //
  new Thread(() => bob.pass(alice)).start() //
}
