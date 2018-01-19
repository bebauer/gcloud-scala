package gcloud.scala.pubsub

import com.google.api.gax.core.CredentialsProvider
import com.google.cloud.pubsub.v1.{Publisher => GCloudPublisher}
import com.google.pubsub.v1
import com.google.pubsub.v1.PubsubMessage

import scala.concurrent.Future

object Publisher {

  def apply(topicName: v1.TopicName): GCloudPublisher = Builder(topicName)

  def apply(
      topicName: v1.TopicName,
      pubSubUrl: PubSubUrl,
      credentialsProvider: CredentialsProvider =
        com.google.cloud.pubsub.v1.TopicAdminSettings.defaultCredentialsProviderBuilder().build()
  ): GCloudPublisher =
    Builder(topicName, pubSubUrl).setCredentialsProvider(credentialsProvider)

  object Builder {
    def apply(topicName: v1.TopicName): GCloudPublisher.Builder =
      GCloudPublisher.newBuilder(topicName)

    def apply(topicName: v1.TopicName, pubSubUrl: PubSubUrl): GCloudPublisher.Builder =
      GCloudPublisher
        .newBuilder(topicName)
        .setChannelProvider(pubSubUrl.channelProviderBuilder().build())
  }

  private[pubsub] object Logic {
    import FutureConversions.Implicits._

    def publishAsync[T](publisher: GCloudPublisher,
                        message: T)(implicit converter: T => PubsubMessage): Future[String] =
      publisher.publish(converter(message))
  }
}
