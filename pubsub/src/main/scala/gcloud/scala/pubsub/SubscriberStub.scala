package gcloud.scala.pubsub

import com.google.cloud.pubsub.v1.stub

object SubscriberStub {
  def apply(settings: stub.SubscriberStubSettings = Settings()): stub.SubscriberStub =
    settings.createStub()

  object Settings {
    def apply(): stub.SubscriberStubSettings.Builder =
      stub.SubscriberStubSettings.newBuilder()

    def apply(pubSubUrl: PubSubUrl,
              maxInboundMessageSize: Option[Int] = None): stub.SubscriberStubSettings.Builder =
      Settings().setTransportChannelProvider(
        maxInboundMessageSize
          .map(pubSubUrl.channelProviderBuilder().maxInboundMessageSize(_).build())
          .getOrElse(pubSubUrl.channelProviderBuilder().build())
      )
  }
}
