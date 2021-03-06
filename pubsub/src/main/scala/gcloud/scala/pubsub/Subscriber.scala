package gcloud.scala.pubsub

import java.util.concurrent.TimeUnit

import com.google.api.gax.core.CredentialsProvider
import com.google.cloud.pubsub.v1.{
  AckReplyConsumer,
  MessageReceiver,
  Subscriber => GCloudSubscriber
}
import com.google.pubsub.v1

object Subscriber {
  private[pubsub] final val MaxInboundMessageSize = 20 * 1024 * 1024 // 20MB API maximum message size.

  type MessageReceiverType = (v1.PubsubMessage, AckReplyConsumer) => Unit

  def apply(
      subscriptionName: v1.ProjectSubscriptionName
  )(receiver: MessageReceiverType): GCloudSubscriber =
    Builder(subscriptionName, MessageReceiverWrapper(receiver))

  def apply(
      subscriptionName: v1.ProjectSubscriptionName,
      pubSubUrl: PubSubUrl = PubSubUrl.DefaultPubSubUrl,
      credentialsProvider: CredentialsProvider =
        com.google.cloud.pubsub.v1.SubscriptionAdminSettings.defaultCredentialsProviderBuilder.build,
      maxInboundMessageSize: Int = MaxInboundMessageSize
  )(receiver: MessageReceiverType): GCloudSubscriber =
    Builder(subscriptionName, MessageReceiverWrapper(receiver))
      .setChannelProviderWithUrl(pubSubUrl, maxInboundMessageSize)
      .setCredentialsProvider(credentialsProvider)

  object Builder {
    def apply(subscriptionName: v1.ProjectSubscriptionName,
              receiver: MessageReceiver): GCloudSubscriber.Builder =
      GCloudSubscriber.newBuilder(subscriptionName, receiver)

    private[pubsub] object Logic {
      def setChannelProviderWithUrl(
          builder: GCloudSubscriber.Builder,
          pubSubUrl: PubSubUrl,
          maxInboundMessageSize: Int = MaxInboundMessageSize
      ): GCloudSubscriber.Builder =
        builder.setChannelProvider(
          pubSubUrl
            .channelProviderBuilder()
            .maxInboundMessageSize(maxInboundMessageSize)
            .keepAliveTime(5, TimeUnit.SECONDS)
            .build()
        )
    }
  }

  private object MessageReceiverWrapper {
    def apply(receiver: MessageReceiverType): MessageReceiverWrapper =
      new MessageReceiverWrapper(receiver)
  }

  private class MessageReceiverWrapper(receiver: MessageReceiverType) extends MessageReceiver {
    override def receiveMessage(message: v1.PubsubMessage, consumer: AckReplyConsumer): Unit =
      receiver(message, consumer)
  }
}
