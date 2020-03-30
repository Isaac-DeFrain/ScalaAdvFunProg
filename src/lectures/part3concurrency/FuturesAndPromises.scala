package lectures.part3concurrency

import com.sun.net.httpserver.Authenticator.Failure

import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Random, Success, Try}
import scala.concurrent.duration._

// important for Futures
import scala.concurrent.ExecutionContext.Implicits.global

object FuturesAndPromises extends App {

  // Futures are a functional way of computing something in parallel or on another thread

  def calculateMeaningOfLife: Int = {
    Thread.sleep(2000)
    42
  }

  val aFuture = Future {
    calculateMeaningOfLife // calculates meaning life on ANOTHER thread
  } // (global) - passed by the compiler

  println(aFuture.value) // Option[Try[Int]]

  println("Waiting on the future...")
  aFuture.onComplete(t => t match {
    case Success(value) => println(value)
    case _              => println("The future is now.")
  }) // SOME thread -- no guarantees

  Thread.sleep(3000) // makes sure Future finishes before MAIN thread

  // mini social network - asynchronous
  case class Profile(id: String, name: String) {
    def poke(anotherProfile: Profile): Unit = {
      println(s"${this.name} poking ${anotherProfile.name}")
    }
  }

  object SocialNetwork {
    // "database"
    val names = Map(
      "fb.id.1-zuck"  -> "Mark",
      "fb.id.2-bill"  -> "Bill",
      "fb.id.0-dummy" -> "dummy"
    )
    val friends = Map(
      "fb.id.1-zuck" -> "fb.id.2-bill"
    )

    val random = new Random()

    def fetchProfile(id: String): Future[Profile] = Future {
      // fetching from the DB
      Thread.sleep(random.nextInt(300))
      Profile(id, names(id))
    }

    def getBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(random.nextInt(300))
      val bfId = friends(profile.id)
      Profile(bfId, names(bfId))
    }
  }

  // client: Mark poke Bill
  val mark = SocialNetwork.fetchProfile("fb.id.1-zuck")
//  mark.onComplete {
//    case Success(markProfile) => {
//      val bill = SocialNetwork.getBestFriend(markProfile)
//      bill.onComplete {
//        case Success(billProfile) => markProfile.poke(billProfile)
//        case Failure(ex) => ex.printStackTrace()
//      }
//    }
//    case Failure(ex) => ex.printStackTrace()
//  } // not sure why this code is throwing *Failure* ambiguity errors

  // Functional composition of Futures
  // map, flatMap, filter

  // map-ping Futures
  val nameOnTheWall = mark.map(_.name)
  // flatMap-ping Futures
  val marksBestFriend = mark.flatMap(SocialNetwork.getBestFriend(_))
  // filter-ing Futures
  val zucksBestFriendRestricted = marksBestFriend.filter(_.name.startsWith("Z"))

//  println(nameOnTheWall)
//  println(marksBestFriend)
//  println(zucksBestFriendRestricted)

  // for-comprehensions
  for {
    mark <- SocialNetwork.fetchProfile("fb.id.1-zuck")
    bill <- SocialNetwork.getBestFriend(mark)
  } mark.poke(bill)

  Thread.sleep(1000)

  // Fallbacks
  // recover
  val aProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recover {
    case e: Throwable => Profile("fb.id.0-dummy", "Forever alone")
  }

  // recoverWith
  val aFetchedProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recover {
    case e: Throwable => SocialNetwork.fetchProfile("fb.id.0-dummy")
  }

  // fallbackTo
  // If first Future succeeds, that result is used.
  // If first Future fails, try second Future.
  //  - If second Future succeeds, that result is used.
  //  - If second Future fails, the Failure from the first is used.
  val fallbackResult = SocialNetwork.fetchProfile("unknown id").fallbackTo {
    SocialNetwork.fetchProfile("fb,id.0-dummy")
  }

  // Blocking on Futures

  // Await.result
  // online banking app
  case class User(name: String)
  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    val name: String = "Quantifier Bank"

    // api
    def fetchUser(username: String): Future[User] = Future {
      // simulate fetching user from DB
      Thread.sleep(500)
      User(username)
    }
    def createTransaction(user: User, merchant: String, amount: Double): Future[Transaction] = Future {
      // simulate some verification processes
      Thread.sleep(1000)
      Transaction(user.name, merchant, amount, "Success!!!")
    }
    def purchase(username: String, item: String, merchant: String, cost: Double): String = {
      // fetch user from DB
      // create a transaction
      // WAIT for the transaction to finish
      val transactionStatusFuture = for {
        user <- fetchUser(username)
        txn  <- createTransaction(user, merchant, cost)
      } yield txn.status

      Await.result(transactionStatusFuture, 2.seconds) // implicit conversions -> pimp my library
    }
  }

  println(BankingApp.purchase("Isaac", "burrito", "Quantifier Grill", 12))

  // Promises
  // Futures are the functional way of composing non-blocking computations
  // Futures are read-only when they are done

  val promise = Promise[Int]() // "controller" over future
  val future = promise.future

  // thread 1 - "consumer"
  future.onComplete {
    case Success(res) => println("[consumer] I've received " + res)
  }

  // thread 2 - "producer"
  val producer = new Thread(() => {
    println("[producer] hard at work...")
    Thread.sleep(500)
    // fulfilling the promise
    promise.success(42)
    // promise.failure(...) // for a failure
    println("[producer] done!")
  })

  producer.start()
  Thread.sleep(1000)

  /*
    Exercises:
    1. fulfill a future IMMEDIATELY with a value
    2. two futures in sequence: inSequence(fa, fb)
    3. earlyBird(fa, fb) => new future with the first value of the futures
    4. last(fa, fb) => new future with last value of the futures
    5. retryUnitl(action: () => Future[T], cond: T => Boolean): Future[T]
   */

  // 1. immediately fulfill a future
  def fulfillImmediately[T](value: T): Future[T] = Future(value)

  // 2. sequential futures
  def inSeq[A, B](fa: Future[A], fb: Future[B]): Future[B] =
    fa.flatMap(_ => fb)

  // 3. first value
  def earlier[A](fa: Future[A], fb: Future[A]): Future[A] = {
    val promise = Promise[A]
    fa.onComplete(promise.tryComplete)
    fb.onComplete(promise.tryComplete)
    promise.future
    // promise.tryComplete == { case Success(v) => try { promise.success(v) } catch { _ =>  }
    //                          case Failure(e) => try { promise.failure(e) } catch { _ =>  } }
  }

  val fa = Future {
    Thread.sleep(500)
    42
  }
  val fb = Future(43)
  // earlier(fa, fb).onComplete(println(_))

  // 4. later of two futures
  def later[A](fa: Future[A], fb: Future[A]): Future[A] = {
    // 1st promise both futures will try to complete
    // 2nd promise only the last future will try to complete
    val bothPromise = Promise[A]
    val lastPromise = Promise[A]
    val checkAndComplete = (res: Try[A]) =>
      if (!bothPromise.tryComplete(res))
        lastPromise.complete(res)
    fa.onComplete(checkAndComplete)
    fb.onComplete(checkAndComplete)
    lastPromise.future
  }

  earlier(fa, fb).foreach(e => println("EARLIER: " + e))
  later(fa, fb).foreach(l => println("LATER: " + l))
  Thread.sleep(1000)

  // 5. retry until a condition is satisfied
  def retryUntil[A](action: () => Future[A], cond: A => Boolean): Future[A] = {
    action()
      .filter(cond)
      .recoverWith {
        case _ => retryUntil(action, cond)
      }
  }

  val random = new Random()
  val action = () => Future {
    Thread.sleep(100)
    val nextValue = random.nextInt(100)
    println("generated " + nextValue)
    nextValue
  }

  retryUntil(action, (x: Int) => x < 10).foreach(x => println("settled at " + x))
  Thread.sleep(10000)

  /*
    Future[T] is a computation that will finish at *some point*
    - import default ExecutionContext and passed it implicitly

    Non-blocking processing -- future.onComplete {...}

    Future is a monad -- map, flatMap, filter, for-comprehensions

    Fallbacks and recovery

    Blocking if needed -- Await.result(Awaitable, Duration)

    Promises
    - Futures are immutable, "read-only" objects
    - Promises are "writable-once" containers/managers over a future
    - thread 1:                           - thread 2:
      - creates an empty promise            - holds the promise
      - knows how to handle the result      - fulfills or fails the promise by calling: success, failure, or complete
   */
}
