package gcloud.scala.pubsub

import com.google.cloud.pubsub.v1

object TopicAdminSettings {
  def apply(): v1.TopicAdminSettings.Builder =
    v1.TopicAdminSettings.newBuilder()

  def apply(pubSubUrl: PubSubUrl,
            maxInboundMessageSize: Option[Int] = None): v1.TopicAdminSettings.Builder =
    TopicAdminSettings().setTransportChannelProvider(
      maxInboundMessageSize
        .map(pubSubUrl.channelProviderBuilder().maxInboundMessageSize(_).build())
        .getOrElse(pubSubUrl.channelProviderBuilder().build())
    )
}
