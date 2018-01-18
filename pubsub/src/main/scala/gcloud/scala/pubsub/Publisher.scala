package gcloud.scala.pubsub

import com.google.pubsub.v1
import com.google.pubsub.v1.PubsubMessage
import com.google.cloud.pubsub.v1.{Publisher => GCloudPublisher}

import scala.concurrent.Future

object Publisher {

  def apply(topicName: v1.TopicName): GCloudPublisher =
    GCloudPublisher.newBuilder(topicName)

  def apply(topicName: v1.TopicName, pubSubUrl: PubSubUrl): GCloudPublisher =
    GCloudPublisher
      .newBuilder(topicName)
      .setChannelProvider(pubSubUrl.channelProviderBuilder().build())

  private[pubsub] object Logic {
    import FutureConversions.Implicits._

    def publishAsync[T](publisher: GCloudPublisher,
                        message: T)(implicit converter: T => PubsubMessage): Future[String] =
      publisher.publish(converter(message))
  }
}
