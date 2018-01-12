package gcloud.scala.pubsub

import java.util.concurrent.TimeoutException

import gcloud.scala.pubsub.retry.ExponentialBackoff._
import gcloud.scala.pubsub.retry.{RetryAttempt, RetryScheduler}
import io.grpc.StatusRuntimeException

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

private[pubsub] object GrpcCall {
  implicit class FutureExtensions[T](future: Future[T]) {
    def withTimeout(duration: FiniteDuration,
                    exception: Exception = new TimeoutException("Future timed out!"))(
        implicit executionContext: ExecutionContext,
        scheduler: RetryScheduler
    ): Future[T] =
      Future.firstCompletedOf(Seq(future, scheduler.schedule(duration)(Future.failed(exception))))
  }

  def apply[Request, Response](method: => Future[Response], callSettings: CallSettings[Request])(
      implicit executionContext: ExecutionContext,
      scheduler: RetryScheduler
  ): Future[Response] = {
    val attempt = RetryAttempt.first(callSettings.retrySettings)

    callWithRetry(attempt, method, callSettings)
  }

  private def callWithRetry[Request, Response](attempt: RetryAttempt,
                                               call: => Future[Response],
                                               callSettings: CallSettings[Request])(
      implicit executionContext: ExecutionContext,
      scheduler: RetryScheduler
  ): Future[Response] =
    call withTimeout attempt.rpcTimeout recoverWith {
      case sre: StatusRuntimeException
          if callSettings.retryStatusCodes.exists(_.getCode == sre.getStatus.getCode) =>
        val nextAttempt = attempt.next

        if (nextAttempt.shouldRetry) {
          scheduler.schedule(nextAttempt.randomizedDelay)(
            callWithRetry(nextAttempt, call, callSettings)
          )
        } else {
          throw sre
        }
    }
}
