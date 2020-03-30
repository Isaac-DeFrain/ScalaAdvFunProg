package lectures.part4implicits

import java.util.Date

object JSONSerialization extends App {
  /*
    Users, posts, feeds
    serialize to JSON
   */

  case class User(name: String, age: Int, email: String)
  case class Post(content: String, created: Date)
  case class Feed(user: User, posts: List[Post])

  /*
    Want to serialize to JSON using the type class pattern
    Steps:
    1 - intermediate data types: Int, String, List, Date
    2 - type class for conversion to intermediate data types
    3 - serialize intermediate types to JSON
   */

  // 1 - intermediate data type
  sealed trait JSONValue {
    def stringify: String
  }

  final case class JSONString(value: String) extends JSONValue {
    def stringify: String = "\"" + value + "\""
  }

  final case class JSONNumber(value: Int) extends JSONValue {
    def stringify: String = value.toString
  }

  final case class JSONArray(values: List[JSONValue]) extends JSONValue {
    def stringify: String = values.map(_.stringify).mkString("[", ", ", "]")
  }

  final case class JSONObject(values: Map[String, JSONValue]) extends JSONValue {
    /*
      {
        name: "John"
        age: 22
        friends: [...]
        latestPost: {
          content: "Scala Rocks"
          date: ...
        }
      }
     */
    def stringify: String = values.map {
      case (key, value) => "\"" + key + "\": " + value.stringify
    }.mkString("{", ", ", "}")
  }
    // tests
  println(JSONString("Haskell").stringify)
  println(JSONNumber(42).stringify)
  println(JSONArray(List(JSONString("a"), JSONArray(List(JSONNumber(42))))).stringify)
  val data = JSONObject(Map(
    "name" -> JSONString("Isaac"),
    "posts" -> JSONArray(List(
      JSONString("Quantifier!"),
      JSONNumber(666)
    ))
  ))
  println(data.stringify)

  // 2 - type class
  /*
    1. type class
    2. implicit type class instances
    3. pimp library to use type class instances
   */

  // 2.1 - type class for converting to JSONValue
  trait JSONConverter[T] {
    def convert(value: T): JSONValue
  }

  // 2.3 - pimp my library
  implicit class JSONOps[T](value: T) {
    def toJSON(implicit converter: JSONConverter[T]): JSONValue =
      converter.convert(value)
  }

  // 2.2 - implicit instances
  implicit object StringConverter extends JSONConverter[String] {
    def convert(value: String): JSONValue = JSONString(value)
  }

  implicit object NumberConverter extends JSONConverter[Int] {
    def convert(value: Int): JSONValue = JSONNumber(value)
  }

  implicit object UserConverter extends JSONConverter[User] {
    def convert(user: User): JSONValue = JSONObject(Map(
      "name" -> JSONString(user.name),
      "age" -> JSONNumber(user.age),
      "email" -> JSONString(user.email)
    ))
  }

  implicit object PostConverter extends JSONConverter[Post] {
    def convert(post: Post): JSONValue = JSONObject(Map(
      "content" -> JSONString(post.content),
      "created" -> JSONString(post.created.toString)
    ))
  }

  implicit object FeedConverter extends JSONConverter[Feed] {
    def convert(feed: Feed): JSONValue = JSONObject(Map(
      "user" -> feed.user.toJSON,
      "posts" -> JSONArray(feed.posts.map(_.toJSON))
    ))
  }

  // 3 - stringify the result
  val now = new Date(System.currentTimeMillis())
  val isaac = User("Isaac", 32, "isaac@email.com")
  val feed = Feed(isaac, List(
    Post("Haskell!", now),
    Post("Scala!", now),
    Post("Scheme!", now)
  ))

  println(feed.toJSON.stringify)

}

