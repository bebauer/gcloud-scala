package gcloud.scala.pubsub.retry

import scala.concurrent.duration.{Duration, FiniteDuration}

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
                        retryDelay: FiniteDuration,
                        rpcTimeout: FiniteDuration,
                        randomizedDelay: FiniteDuration,
                        attempts: Int,
                        firstAttemptStartTimeNanos: Long) {}
