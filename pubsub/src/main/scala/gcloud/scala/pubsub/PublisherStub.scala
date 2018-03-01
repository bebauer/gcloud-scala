package gcloud.scala.pubsub

import com.google.cloud.pubsub.v1.stub
import com.google.pubsub.v1

import scala.collection.JavaConverters._

object PublisherStub {
  def apply(settings: stub.PublisherStubSettings = Settings()): stub.PublisherStub =
    settings.createStub()

  def apply(pubSubUrl: PubSubUrl): stub.PublisherStub = PublisherStub(Settings(pubSubUrl))

  object Settings {
    def apply(): stub.PublisherStubSettings.Builder =
      stub.PublisherStubSettings.newBuilder()

    def apply(pubSubUrl: PubSubUrl): stub.PublisherStubSettings.Builder =
      Settings().setTransportChannelProvider(pubSubUrl.channelProviderBuilder().build())
  }

  object PublishRequest {
    def apply[T](topic: v1.TopicName,
                 messages: T*)(implicit converter: T => v1.PubsubMessage): v1.PublishRequest =
      v1.PublishRequest
        .newBuilder()
        .setTopic(topic.fullName)
        .addAllMessages(messages.map(converter).asJava)
        .build()
  }
}
