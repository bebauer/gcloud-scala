package gcloud.scala.pubsub

import com.google.cloud.pubsub.v1

object SubscriptionAdminSettings {
  def apply(): v1.SubscriptionAdminSettings.Builder =
    v1.SubscriptionAdminSettings.newBuilder()

  def apply(pubSubUrl: PubSubUrl,
            maxInboundMessageSize: Option[Int] = None): v1.SubscriptionAdminSettings.Builder =
    SubscriptionAdminSettings().setTransportChannelProvider(
      maxInboundMessageSize
        .map(pubSubUrl.channelProviderBuilder().maxInboundMessageSize(_).build())
        .getOrElse(pubSubUrl.channelProviderBuilder().build())
    )
}
