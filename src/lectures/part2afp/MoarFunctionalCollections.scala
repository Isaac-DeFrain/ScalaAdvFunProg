package lectures.part2afp

object MoarFunctionalCollections extends App {

  /**
   * Sequences are callable (i.e. have an apply method) through an integer index.
   * Sequences are partial functions!
   *
   * trait Seq[+A] extends PartialFunction[Int, A] {
   *  def apply(index: Int): A
   * }
   */

  /**
   * Maps are callable through their keys.
   * Maps are partial functions!
   *
   * trait Map[A, +B] extends PartialFunction[A, B] {
   *  def apply(key: A): B
   *  def get(key: A): Option[B] // safe apply
   * }
   */
}
