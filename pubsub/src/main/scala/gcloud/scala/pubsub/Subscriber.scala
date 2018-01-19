package gcloud.scala.pubsub

import java.util.concurrent.TimeUnit

import com.google.api.gax.core.CredentialsProvider
import com.google.cloud.pubsub.v1.{
  AckReplyConsumer,
  MessageReceiver,
  Subscriber => GCloudSubscriber
}
import com.google.pubsub.v1
import com.google.pubsub.v1.PubsubMessage

object Subscriber {
  private final val MaxInboundMessageSize = 20 * 1024 * 1024 // 20MB API maximum message size.

  type MessageReceiverType = (PubsubMessage, AckReplyConsumer) => Unit

  def apply(
      subscriptionName: v1.SubscriptionName
  )(receiver: MessageReceiverType): GCloudSubscriber =
    Builder(subscriptionName, MessageReceiverWrapper(receiver))

  def apply(
      subscriptionName: v1.SubscriptionName,
      pubSubUrl: PubSubUrl = PubSubUrl.DefaultPubSubUrl,
      credentialsProvider: CredentialsProvider =
        com.google.cloud.pubsub.v1.SubscriptionAdminSettings.defaultCredentialsProviderBuilder.build,
      maxInboundMessageSize: Int = MaxInboundMessageSize
  )(receiver: MessageReceiverType): GCloudSubscriber =
    Builder(subscriptionName, MessageReceiverWrapper(receiver))
      .setChannelProvider(
        pubSubUrl
          .channelProviderBuilder()
          .maxInboundMessageSize(maxInboundMessageSize)
          .keepAliveTime(5, TimeUnit.SECONDS)
          .build()
      )
      .setCredentialsProvider(credentialsProvider)

  object Builder {
    def apply(subscriptionName: v1.SubscriptionName,
              receiver: MessageReceiver): GCloudSubscriber.Builder =
      GCloudSubscriber.newBuilder(subscriptionName, receiver)
  }

  private object MessageReceiverWrapper {
    def apply(receiver: MessageReceiverType): MessageReceiverWrapper =
      new MessageReceiverWrapper(receiver)
  }

  private class MessageReceiverWrapper(receiver: MessageReceiverType) extends MessageReceiver {
    override def receiveMessage(message: PubsubMessage, consumer: AckReplyConsumer): Unit =
      receiver(message, consumer)
  }
}
