package gcloud.scala.pubsub

import java.util.concurrent.TimeoutException

import gcloud.scala.pubsub.PubSubClientConfig.CallSettings
import gcloud.scala.pubsub.retry.ExponentialBackoff._
import gcloud.scala.pubsub.retry.{RetryAttempt, RetryScheduler}
import io.grpc.StatusRuntimeException

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

object GrpcCall {
  implicit class FutureExtensions[T](future: Future[T]) {
    def withTimeout(duration: Duration,
                    exception: Exception = new TimeoutException("Future timed out!"))(
        implicit executionContext: ExecutionContext,
        scheduler: RetryScheduler
    ): Future[T] =
      Future.firstCompletedOf(Seq(future, scheduler.schedule(duration)(Future.failed(exception))))
  }

  def apply[T](callSettings: CallSettings)(call: => Future[T])(
      implicit executionContext: ExecutionContext,
      scheduler: RetryScheduler
  ): Future[T] = {
    val attempt = RetryAttempt.first(callSettings.retrySettings)

    callWithRetry(callSettings, attempt, call)
  }

  private def callWithRetry[T](callSettings: CallSettings, attempt: RetryAttempt, call: => Future[T])(
      implicit executionContext: ExecutionContext,
      scheduler: RetryScheduler
  ): Future[T] =
    call withTimeout attempt.rpcTimeout recoverWith {
      case sre: StatusRuntimeException
          if callSettings.retryStatusCodes.exists(_.getCode == sre.getStatus.getCode) =>
        val nextAttempt = attempt.next

        if (nextAttempt.shouldRetry) {
          scheduler.schedule(nextAttempt.randomizedDelay)(callWithRetry(callSettings, nextAttempt, call))
        } else {
          throw sre
        }
    }
}
