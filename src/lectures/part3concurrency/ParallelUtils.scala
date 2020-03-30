package lectures.part3concurrency

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicReference

import scala.collection.parallel.{ForkJoinTaskSupport, Task, TaskSupport}
import scala.collection.parallel.immutable.ParVector

object ParallelUtils extends App {

  // 1 - parallel collections

  val parList = List(1,2,3).par

  val parVector = ParVector[Int](1,2,3,4)

  // List, Vector, Seq, Array, Map (Hash, Trie), Set (Hash, Trie)

  def measure[T](op: => T): Long = {
    val initTime = System.currentTimeMillis()
    op
    System.currentTimeMillis() - initTime
  }

  val list = (1 to 10000000).toList
  val serTime = measure {
    list.map(_ + 1)
  }
  val parTime = measure {
    list.par.map(_ + 1)
  }
  //println("Cereal time: " + serTime)   // 2258 ms
  //println("Parallel time: " + parTime) // 348  ms
  // for smaller data sets, parallel processing can take longer because it is expensive to start and stop threads

  /*
    Map-reduce model
    - split elements into chunks - Splitter
    - operate on chunks
    - recombine - Combiner
   */

  // map, flatMap, filter, foreach, reduce, fold

  // be careful with fold & reduce - non-associative operators
  println(List(1,2,3).reduce(_ - _))
  println(List(1,2,3).par.reduce(_ - _))

  // synchronization
  var sum = 0
  List(1,2,3,4,5).par.foreach(sum += _)
  println(sum) // 14!?!? -- race condition!!!

  // configuring parallel collections
  parVector.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(2))
  /*
    Alternatives:
    - ThreadPoolTaskSupport - deprecated
    - ExecutionContextTaskSupport(EC) -- can be used with Futures API
   */

  // create custom task support
//  parVector.tasksupport = new TaskSupport {
//    // execute -- schedules a thread to run in parallel, basically
//    override def execute[R, Tp](fjtask: Task[R, Tp]): () => R = ???
//
//    // executeAndWaitResult -- same as execute, except it blocks until a result is available, i.e. waits for the thread to join
//    override def executeAndWaitResult[R, Tp](task: Task[R, Tp]): R = ???
//
//    // parallelismLevel -- # of CPU cores this should run
//    override def parallelismLevel: Int = ???
//
//    // environment -- internal member that manages threads, can be a ForkJoinPool, ExecutionContext, etc.
//    override val environment: AnyRef = ???
//  }

  // 2 - Atomic Operations & References
  val atomic = new AtomicReference[Int](2)

  val currentValue = atomic.get() // thread-safe read
  atomic.set(4)                   // thread-safe write
  atomic.getAndSet(5)   // thread-safe combo

  atomic.compareAndSet(5, 23) // if value is 5, then set to 23 -- reference (shallow) equality
  atomic.updateAndGet(_ + 19) // thread-safe function run -- update value with given function, then get result

  atomic.getAndUpdate(_ + 19) // opposite order as above

  atomic.accumulateAndGet(12, _ + _) // thread-safe accumulation -- does operation with given value, then gets
  atomic.getAndAccumulate(12, _ + _) // opposite order as above

  val atomic1 = new AtomicReference[Int](0)
  val atomic2 = new AtomicReference[Int](0)
  println(atomic1.accumulateAndGet(10, _ + _))
  println(atomic2.getAndAccumulate(10, _ + _))
}
