package gcloud.scala.pubsub.retry

import scala.concurrent.duration.Duration

object RetryAttempt {
  def first(settings: RetrySettings) =
    RetryAttempt(
      settings = settings,
      retryDelay = Duration.Zero,
      rpcTimeout = settings.totalTimeout,
      randomizedDelay = Duration.Zero,
      attempts = 0,
      firstAttemptStartTimeNanos = System.nanoTime()
    )
}

case class RetryAttempt(settings: RetrySettings,
                        retryDelay: Duration,
                        rpcTimeout: Duration,
                        randomizedDelay: Duration,
                        attempts: Int,
                        firstAttemptStartTimeNanos: Long) {}
