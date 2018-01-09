package gcloud.scala.pubsub.retry

import scala.concurrent.duration._

object RetrySettings {
  case class ComplexRetry(initialRetryDelay: FiniteDuration,
                          retryDelayMultiplier: Double,
                          maxRetryDelay: FiniteDuration,
                          initialRpcTimeout: FiniteDuration,
                          rpcTimeoutMultiplier: Double,
                          maxRpcTimeout: FiniteDuration,
                          totalTimeout: FiniteDuration)
      extends RetrySettings

  case class SimpleTimeout(timeout: FiniteDuration) extends RetrySettings {
    override val initialRetryDelay: FiniteDuration = 0.seconds
    override val retryDelayMultiplier: Double      = 1
    override val maxRetryDelay: FiniteDuration     = 0.seconds
    override val initialRpcTimeout: FiniteDuration = timeout
    override val rpcTimeoutMultiplier: Double      = 1
    override val maxRpcTimeout: FiniteDuration     = timeout
    override val totalTimeout: FiniteDuration      = timeout
    override val maxAttempts: Option[Int]          = Some(1)
  }
}

trait RetrySettings {
  val initialRetryDelay: FiniteDuration
  val retryDelayMultiplier: Double
  val maxRetryDelay: FiniteDuration
  val initialRpcTimeout: FiniteDuration
  val rpcTimeoutMultiplier: Double
  val maxRpcTimeout: FiniteDuration
  val totalTimeout: FiniteDuration
  val maxAttempts: Option[Int] = None
}
