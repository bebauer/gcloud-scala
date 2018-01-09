package gcloud.scala.pubsub

import com.google.auth.Credentials
import com.google.auth.oauth2.GoogleCredentials
import gcloud.scala.pubsub.PubSubClientConfig.CallSettings
import gcloud.scala.pubsub.retry.RetrySettings.ComplexRetry
import gcloud.scala.pubsub.retry.{RetryScheduler, RetrySettings}
import io.grpc.netty.{NegotiationType, NettyChannelBuilder}
import io.grpc.{ManagedChannel, Status}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object PubSubClientConfig {
  import scala.collection.JavaConverters._

  type CredentialsProvider = () => Credentials

  trait ChannelProvider {
    def channel(maybeExecutor: Option[ExecutionContextExecutor] = None): ClientChannel
  }

  case class ClientChannel(channel: ManagedChannel, executor: ExecutionContextExecutor)

  case class DefaultChannelProvider(url: PubSubUrl = PubSubUrl.DefaultPubSubUrl,
                                    maxInboundMessageSize: Int = DefaultMaxInboundMessageSize)
      extends ChannelProvider {
    override def channel(maybeExecutor: Option[ExecutionContextExecutor]): ClientChannel =
      maybeExecutor match {
        case Some(executor) =>
          ClientChannel(
            channel = NettyChannelBuilder
              .forAddress(url.host, url.port)
              .maxInboundMessageSize(maxInboundMessageSize)
              .flowControlWindow(5000000)
              .negotiationType(
                if (url.tlsEnabled) NegotiationType.TLS
                else NegotiationType.PLAINTEXT
              )
              .executor(executor)
              .build(),
            executor = executor
          )
        case None =>
          throw new IllegalArgumentException(
            "An executor has to be defined for the default channel provider."
          )
      }
  }

  case class CallSettings(retryStatusCodes: Set[Status], retrySettings: RetrySettings)

  final val DefaultServiceScopes =
    List("https://www.googleapis.com/auth/cloud-platform", "https://www.googleapis.com/auth/pubsub")

  final val DefaultMaxInboundMessageSize = 4 * 1024 * 1024 // 4MB

  final val DefaultCredentialsProvider = () =>
    GoogleCredentials.getApplicationDefault.createScoped(DefaultServiceScopes.asJava)

  def apply(url: PubSubUrl): PubSubClientConfig =
    apply(url, DefaultMaxInboundMessageSize)

  def apply(url: PubSubUrl, maxInboundMessageSize: Int): PubSubClientConfig =
    new PubSubClientConfig(DefaultChannelProvider(url, maxInboundMessageSize))

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

  final val RetryableWithDefaultCallSettings = CallSettings(
    RetryableCallStatusCodes,
    DefaultRetrySettings
  )
}

case class PubSubClientConfig(
    channelProvider: PubSubClientConfig.ChannelProvider =
      PubSubClientConfig.DefaultChannelProvider(),
    credentialsProvider: PubSubClientConfig.CredentialsProvider =
      PubSubClientConfig.DefaultCredentialsProvider,
    retryScheduler: RetryScheduler = RetryScheduler(),
    createSubscriptionSettings: CallSettings = PubSubClientConfig.RetryableWithDefaultCallSettings,
    updateSubscriptionSettings: CallSettings = PubSubClientConfig.RetryableWithDefaultCallSettings,
    listSubscriptionSettings: CallSettings = PubSubClientConfig.RetryableWithDefaultCallSettings,
    deleteSubscriptionSettings: CallSettings = PubSubClientConfig.RetryableWithDefaultCallSettings,
    pullSettings: CallSettings = CallSettings(PubSubClientConfig.PullCallRetryStatusCodes,
                                              PubSubClientConfig.MessagingRetrySettings),
    acknowledgeSettings: CallSettings = CallSettings(PubSubClientConfig.NonRetryableCallStatusCodes,
                                                     PubSubClientConfig.MessagingRetrySettings),
    modifyAckDeadlineSettings: CallSettings =
      CallSettings(PubSubClientConfig.NonRetryableCallStatusCodes,
                   PubSubClientConfig.DefaultRetrySettings),
    modifyPushConfigSettings: CallSettings = CallSettings(
      PubSubClientConfig.NonRetryableCallStatusCodes,
      PubSubClientConfig.DefaultRetrySettings
    ),
    listTopicsSettings: CallSettings = PubSubClientConfig.RetryableWithDefaultCallSettings,
    createTopicSettings: CallSettings = PubSubClientConfig.RetryableWithDefaultCallSettings,
    updateTopicSettings: CallSettings = PubSubClientConfig.RetryableWithDefaultCallSettings,
    deleteTopicSettings: CallSettings = PubSubClientConfig.RetryableWithDefaultCallSettings,
    listTopicSubscriptionsSettings: CallSettings =
      PubSubClientConfig.RetryableWithDefaultCallSettings,
    publishSettings: CallSettings = CallSettings(PubSubClientConfig.PublishCallRetryStatusCodes,
                                                 PubSubClientConfig.MessagingRetrySettings)
)
