package gcloud.scala.pubsub

import com.google.pubsub.v1._
import gcloud.scala.pubsub.retry.RetrySettings
import gcloud.scala.pubsub.retry.RetrySettings.ComplexRetry
import io.grpc.Status

import scala.concurrent.duration._

object CallSettings {
  final val RetryableCallStatusCodes = Set(Status.DEADLINE_EXCEEDED, Status.UNAVAILABLE)

  final val NonRetryableCallStatusCodes: Set[Status] = Set()

  final val PullCallRetryStatusCodes = Set(Status.CANCELLED,
                                           Status.DEADLINE_EXCEEDED,
                                           Status.INTERNAL,
                                           Status.RESOURCE_EXHAUSTED,
                                           Status.UNAVAILABLE)

  final val PublishCallRetryStatusCodes = Set(Status.ABORTED,
                                              Status.CANCELLED,
                                              Status.DEADLINE_EXCEEDED,
                                              Status.INTERNAL,
                                              Status.RESOURCE_EXHAUSTED,
                                              Status.UNAVAILABLE,
                                              Status.UNKNOWN)

  final val DefaultRetrySettings =
    ComplexRetry(
      initialRetryDelay = 100.milliseconds,
      retryDelayMultiplier = 1.3,
      maxRetryDelay = 60.seconds,
      initialRpcTimeout = 60.seconds,
      rpcTimeoutMultiplier = 1,
      maxRpcTimeout = 60.seconds,
      totalTimeout = 10.minutes
    )

  final val MessagingRetrySettings = ComplexRetry(
    initialRetryDelay = 100.milliseconds,
    retryDelayMultiplier = 1.3,
    maxRetryDelay = 60.seconds,
    initialRpcTimeout = 12.seconds,
    rpcTimeoutMultiplier = 1,
    maxRpcTimeout = 12.seconds,
    totalTimeout = 10.minutes
  )

  def retryableWithDefaultCallSettings[T]: CallSettings[T] = CallSettings[T](
    RetryableCallStatusCodes,
    DefaultRetrySettings
  )

  implicit val createSubscriptionSettings: CallSettings[Subscription] =
    retryableWithDefaultCallSettings
  implicit val updateSubscriptionSettings: CallSettings[UpdateSubscriptionRequest] =
    retryableWithDefaultCallSettings
  implicit val listSubscriptionSettings: CallSettings[ListSubscriptionsRequest] =
    retryableWithDefaultCallSettings
  implicit val deleteSubscriptionSettings: CallSettings[DeleteSubscriptionRequest] =
    retryableWithDefaultCallSettings
  implicit val pullSettings: CallSettings[PullRequest] =
    CallSettings(PullCallRetryStatusCodes, MessagingRetrySettings)
  implicit val acknowledgeSettings: CallSettings[AcknowledgeRequest] =
    CallSettings(NonRetryableCallStatusCodes, MessagingRetrySettings)
  implicit val modifyAckDeadlineSettings: CallSettings[ModifyAckDeadlineRequest] =
    CallSettings(NonRetryableCallStatusCodes, DefaultRetrySettings)
  implicit val modifyPushConfigSettings: CallSettings[ModifyPushConfigRequest] =
    CallSettings(NonRetryableCallStatusCodes, DefaultRetrySettings)
  implicit val listTopicsSettings: CallSettings[ListTopicsRequest] =
    retryableWithDefaultCallSettings
  implicit val createTopicSettings: CallSettings[Topic] = retryableWithDefaultCallSettings
  implicit val updateTopicSettings: CallSettings[UpdateTopicRequest] =
    retryableWithDefaultCallSettings
  implicit val deleteTopicSettings: CallSettings[DeleteTopicRequest] =
    retryableWithDefaultCallSettings
  implicit val listTopicSubscriptionsSettings: CallSettings[ListTopicSubscriptionsRequest] =
    retryableWithDefaultCallSettings
  implicit val publishSettings: CallSettings[PublishRequest] =
    CallSettings(PublishCallRetryStatusCodes, MessagingRetrySettings)
}

case class CallSettings[T](retryStatusCodes: Set[Status], retrySettings: RetrySettings)
