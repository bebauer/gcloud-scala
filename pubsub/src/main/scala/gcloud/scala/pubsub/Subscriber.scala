package gcloud.scala.pubsub

import java.util.concurrent.TimeUnit

import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.pubsub.v1

object Subscriber {
  private final val MaxInboundMessageSize = 20 * 1024 * 1024 // 20MB API maximum message size.

  def apply(subscriptionName: v1.SubscriptionName,
            messageReceiver: MessageReceiver): com.google.cloud.pubsub.v1.Subscriber =
    com.google.cloud.pubsub.v1.Subscriber
      .newBuilder(subscriptionName, messageReceiver)

  def apply(
      subscriptionName: v1.SubscriptionName,
      messageReceiver: MessageReceiver,
      pubSubUrl: PubSubUrl = PubSubUrl.DefaultPubSubUrl,
      maxInboundMessageSize: Int = MaxInboundMessageSize
  ): com.google.cloud.pubsub.v1.Subscriber =
    com.google.cloud.pubsub.v1.Subscriber
      .newBuilder(subscriptionName, messageReceiver)
      .setChannelProvider(
        pubSubUrl
          .channelProviderBuilder()
          .maxInboundMessageSize(maxInboundMessageSize)
          .keepAliveTime(5, TimeUnit.SECONDS)
          .build()
      )
}
