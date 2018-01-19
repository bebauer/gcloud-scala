package gcloud.scala.pubsub.testkit

import scala.language.implicitConversions

private[testkit] object Lazy {

  def lazily[A](f: => A): Lazy[A] = new Lazy(f)

  implicit def evalLazy[A](l: Lazy[A]): A = l()

}

private[testkit] class Lazy[A] private (f: => A) {

  private var option: Option[A] = None

  def apply(): A = option match {
    case Some(a) => a
    case None    => val a = f; option = Some(a); a
  }

  def isEvaluated: Boolean = option.isDefined

  def foreach[U](f: A => U): Unit = option.foreach(f)
}
