package gcloud.scala.pubsub.retry

import scala.concurrent.duration.Duration

object ExponentialBackoff {
  implicit class ExponentialBackoffAttempt(attempt: RetryAttempt) {
    def next: RetryAttempt = {
      val settings = attempt.settings

      var newRetryDelay: Duration = settings.initialRetryDelay
      var newRpcTimeout: Duration = settings.initialRpcTimeout

      if (attempt.attempts > 0) {
        newRetryDelay = attempt.retryDelay * settings.retryDelayMultiplier
        newRetryDelay = newRetryDelay.min(settings.maxRetryDelay)
        newRpcTimeout = attempt.rpcTimeout * settings.rpcTimeoutMultiplier
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
