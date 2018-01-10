package gcloud.scala.pubsub.retry

import scala.concurrent.duration.{Duration, FiniteDuration}

object ExponentialBackoff {
  implicit class ExponentialBackoffAttempt(attempt: RetryAttempt) {
    def next: RetryAttempt = {
      val settings = attempt.settings

      var newRetryDelay: FiniteDuration = settings.initialRetryDelay
      var newRpcTimeout: FiniteDuration = settings.initialRpcTimeout

      if (attempt.attempts > 0) {
        newRetryDelay = attempt.retryDelay * settings.retryDelayMultiplier match {
          case f: FiniteDuration => f
          case _                 => settings.maxRetryDelay
        }
        newRetryDelay = newRetryDelay.min(settings.maxRetryDelay)
        newRpcTimeout = attempt.rpcTimeout * settings.rpcTimeoutMultiplier match {
          case f: FiniteDuration => f
          case _                 => settings.maxRpcTimeout
        }
        newRpcTimeout = newRpcTimeout.min(settings.maxRpcTimeout)
      }

      RetryAttempt(settings,
                   newRetryDelay,
                   newRpcTimeout,
                   newRetryDelay,
                   attempt.attempts + 1,
                   attempt.firstAttemptStartTimeNanos)
    }

    def shouldRetry: Boolean = {
      val settings = attempt.settings

      val totalTimeSpentNanos = System
        .nanoTime() - attempt.firstAttemptStartTimeNanos + attempt.randomizedDelay.toNanos

      totalTimeSpentNanos <= settings.totalTimeout.toNanos && settings.maxAttempts.forall(
        attempt.attempts < _
      )
    }
  }
}
