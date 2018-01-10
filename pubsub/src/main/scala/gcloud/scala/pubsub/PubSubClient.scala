package gcloud.scala.pubsub

import com.google.auth.Credentials
import gcloud.scala.pubsub.retry.RetryScheduler
import io.grpc.Channel

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object PubSubClient {
  def apply()(implicit executor: ExecutionContextExecutor): PubSubClient =
    apply(PubSubUrl.DefaultPubSubUrl)

  def apply(pubSubUrl: PubSubUrl)(implicit executor: ExecutionContextExecutor): PubSubClient =
    apply(PubSubClientConfig(pubSubUrl))

  def apply(config: PubSubClientConfig)(implicit executor: ExecutionContextExecutor): PubSubClient =
    new DefaultClient(config, Some(executor))

  private class DefaultClient(config: PubSubClientConfig,
                              executor: Option[ExecutionContextExecutor])
      extends PubSubClient {

    private val clientChannel = config.channelProvider.channel(executor)

    private val credentials = config.credentialsProvider()

    override def getChannel: Channel = clientChannel.channel

    override def getCredentials: Credentials = credentials

    override implicit val executionContext: ExecutionContext = clientChannel.executor

    override implicit val retryScheduler: RetryScheduler = config.retryScheduler

    override def close(): Unit =
      clientChannel.channel.shutdown()

    override val createSubscriptionSettings: PubSubClientConfig.CallSettings =
      config.createSubscriptionSettings
    override val updateSubscriptionSettings: PubSubClientConfig.CallSettings =
      config.updateSubscriptionSettings
    override val listSubscriptionSettings: PubSubClientConfig.CallSettings =
      config.listSubscriptionSettings
    override val deleteSubscriptionSettings: PubSubClientConfig.CallSettings =
      config.deleteSubscriptionSettings
    override val pullSettings: PubSubClientConfig.CallSettings        = config.pullSettings
    override val acknowledgeSettings: PubSubClientConfig.CallSettings = config.acknowledgeSettings
    override val modifyAckDeadlineSettings: PubSubClientConfig.CallSettings =
      config.modifyAckDeadlineSettings
    override val modifyPushConfigSettings: PubSubClientConfig.CallSettings =
      config.modifyPushConfigSettings
    override val listTopicsSettings: PubSubClientConfig.CallSettings  = config.listTopicsSettings
    override val createTopicSettings: PubSubClientConfig.CallSettings = config.createTopicSettings
    override val updateTopicSettings: PubSubClientConfig.CallSettings = config.updateTopicSettings
    override val deleteTopicSettings: PubSubClientConfig.CallSettings = config.deleteTopicSettings
    override val listTopicSubscriptionsSettings: PubSubClientConfig.CallSettings =
      config.listTopicSubscriptionsSettings
    override val publishSettings: PubSubClientConfig.CallSettings = config.publishSettings
  }
}

trait PubSubClient extends PubSubPublisher with PubSubSubscriber
