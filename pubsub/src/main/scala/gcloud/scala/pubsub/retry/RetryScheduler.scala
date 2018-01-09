package gcloud.scala.pubsub.retry

import java.util.concurrent.Executors

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Promise}

object RetryScheduler {
  def apply(poolSize: Int = 4): RetryScheduler =
    new ScheduledExecutorServiceRetryScheduler(poolSize)

  private class ScheduledExecutorServiceRetryScheduler(poolSize: Int) extends RetryScheduler {

    private val scheduler = Executors.newScheduledThreadPool(poolSize)

    override def schedule[T](after: Duration)(task: => Future[T]): Future[T] = {
      val promise = Promise[T]()

      scheduler.schedule(() => promise.completeWith(task), after.length, after.unit)

      promise.future
    }
  }
}

trait RetryScheduler {
  def schedule[T](after: Duration)(task: => Future[T]): Future[T]
}
