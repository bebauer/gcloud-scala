package gcloud.scala

import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider
import com.google.api.gax.rpc.UnaryCallable
import com.google.cloud.pubsub.v1.stub
import com.google.cloud.pubsub.{v1 => gcv1}
import com.google.protobuf.{ByteString, Empty, FieldMask}
import com.google.pubsub.v1
import com.google.pubsub.v1._
import gcloud.scala.pubsub.PubSubMessage.MessageDataEncoder
import org.threeten.bp.Duration

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions


package object pubsub {

  import FutureConversions._

  /**
    * Implicitly converts a string to a [[ProjectName]] by calling [[ProjectName.apply()]].
    *
    * @param name the name
    * @return the [[ProjectName]]
    */
  implicit def projectFromString(name: String): ProjectName = ProjectName(name)

  /**
    * Implicitly converts a string to a [[TopicName]] by calling [[TopicName.apply()]].
    *
    * @param fullName the full name
    * @return the [[TopicName]]
    */
  implicit def topicFromString(fullName: String): TopicName = TopicName(fullName)

  implicit class TopicNameExtensions(val topicName: TopicName) extends AnyVal {
    def fullName: String = topicName.toString
  }

  /**
    * Implicitly converts a string to a [[SubscriptionName]] by calling [[SubscriptionName.apply()]].
    *
    * @param fullName the full name
    * @return the [[SubscriptionName]]
    */
  implicit def subscriptionFromString(fullName: String): SubscriptionName =
    SubscriptionName(fullName)

  implicit class SubscriptionNameExtensions(val subscriptionName: v1.SubscriptionName)
      extends AnyVal {
    def fullName: String = subscriptionName.toString
  }

  implicit def subscriptionAdminSettingsBuilderToInstance(
      builder: gcv1.SubscriptionAdminSettings.Builder
  ): gcv1.SubscriptionAdminSettings = builder.build()

  implicit def topicAdminSettingsBuilderToInstance(
      builder: gcv1.TopicAdminSettings.Builder
  ): gcv1.TopicAdminSettings = builder.build()

  implicit class SubscriptionAdminClientExtensions(val client: gcv1.SubscriptionAdminClient)
      extends AnyVal {
    def getSubscriptionAsync(subscriptionName: SubscriptionName): Future[Option[Subscription]] =
      SubscriptionAdminClient.Logic.getSubscriptionAsync(client, subscriptionName)

    def createSubscriptionAsync(subscription: Subscription): Future[Subscription] =
      SubscriptionAdminClient.Logic.createSubscriptionAsync(client, subscription)

    def updateSubscriptionAsync(subscription: Subscription,
                                updateMask: Option[FieldMask] = None): Future[Subscription] =
      SubscriptionAdminClient.Logic.updateSubscriptionAsync(client, subscription, updateMask)

    def deleteSubscriptionAsync(subscriptionName: SubscriptionName): Future[Empty] =
      SubscriptionAdminClient.Logic.deleteSubscriptionAsync(client, subscriptionName)

    def listSubscriptionsAsync(
        projectName: ProjectName,
        pageSize: Option[Int] = None,
        pageToken: Option[String] = None
    ): Future[ListSubscriptionsResponse] =
      SubscriptionAdminClient.Logic.listSubscriptionsAsync(client, projectName, pageSize, pageToken)

    def modifyPushConfigAsync(subscriptionName: SubscriptionName,
                              pushConfig: PushConfig): Future[Empty] =
      SubscriptionAdminClient.Logic.modifyPushConfigAsync(client, subscriptionName, pushConfig)
  }

  implicit class TopicAdminClientExtensions(val client: gcv1.TopicAdminClient) extends AnyVal {
    def listTopicsAsync(
        projectName: ProjectName,
        pageSize: Option[Int] = None,
        pageToken: Option[String] = None
    ): Future[ListTopicsResponse] =
      TopicAdminClient.Logic.listTopicsAsync(client, projectName, pageSize, pageToken)

    def createTopicAsync(topic: Topic): Future[Topic] =
      TopicAdminClient.Logic.createTopicAsync(client, topic)

    def getTopicAsync(topicName: TopicName): Future[Option[Topic]] =
      TopicAdminClient.Logic.getTopicAsync(client, topicName)

    def deleteTopicAsync(topicName: TopicName): Future[Empty] =
      TopicAdminClient.Logic.deleteTopicAsync(client, topicName)

    def listTopicSubscriptionsAsync(
        topicName: TopicName,
        pageSize: Option[Int] = None,
        pageToken: Option[String] = None
    ): Future[ListTopicSubscriptionsResponse] =
      TopicAdminClient.Logic.listTopicSubscriptionsAsync(client, topicName, pageSize, pageToken)
  }

  implicit class ListTopicResponseExtensions(val listTopicsResponse: ListTopicsResponse)
      extends AnyVal {
    def topics: Seq[Topic] = listTopicsResponse.getTopicsList.asScala
  }

  implicit class ListTopicSubscriptionsResponseExtensions(
      val listTopicSubscriptionsResponse: ListTopicSubscriptionsResponse
  ) extends AnyVal {
    def subscriptions: Seq[SubscriptionName] =
      v1.SubscriptionName.parseList(listTopicSubscriptionsResponse.getSubscriptionsList).asScala
  }

  implicit class ListSubscriptionsResponseExtensions(
      val listSubscriptionsResponse: ListSubscriptionsResponse
  ) extends AnyVal {
    def subscriptions: Seq[Subscription] = listSubscriptionsResponse.getSubscriptionsList.asScala
  }

  implicit class PublisherExtensions(val publisher: gcv1.Publisher) extends AnyVal {
    def publishAsync[T](message: T)(implicit converter: T => PubsubMessage): Future[String] =
      Publisher.Logic.publishAsync(publisher, message)
  }

  implicit def subscriptionBuilderToInstance(builder: v1.Subscription.Builder): v1.Subscription =
    builder.build()

  implicit def subscriberBuilderToInstance(builder: gcv1.Subscriber.Builder): gcv1.Subscriber =
    builder.build()

  implicit def publisherBuilderToInstance(builder: gcv1.Publisher.Builder): gcv1.Publisher =
    builder.build()

  implicit def instantiatingChannelProviderBuilderToChannelProviderBuilder(
      builder: InstantiatingGrpcChannelProvider.Builder
  ): ChannelProviderBuilder =
    InstantiatingChannelProviderBuilder(builder)

  implicit val stringToPubSubMessageConverter: String => PubsubMessage = (value: String) =>
    PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(value)).build()

  implicit val pubSubMessageToPubSubMessageConverter: PubsubMessage.Builder => PubsubMessage =
    builder => builder.build()

  type MessageDataDecoder[T] = ByteString => T

  implicit class PubSubMessageExtensions(val message: PubsubMessage) extends AnyVal {
    def dataAs[T](implicit decoder: MessageDataDecoder[T]): T = decoder(message.getData)
  }

  implicit val stringEncoder: MessageDataEncoder[String] = value => ByteString.copyFromUtf8(value)

  implicit class SubscriberBuilderExtensions(val builder: gcv1.Subscriber.Builder) extends AnyVal {
    def setChannelProviderWithUrl(
        pubSubUrl: PubSubUrl,
        maxInboundMessageSize: Int = Subscriber.MaxInboundMessageSize
    ): gcv1.Subscriber.Builder =
      Subscriber.Builder.Logic.setChannelProviderWithUrl(builder, pubSubUrl, maxInboundMessageSize)
  }

  implicit def finiteDurationToBpDuration(duration: FiniteDuration): Duration =
    Duration.ofNanos(duration.toNanos)

  implicit def subscriberStubSettingsToInstance(
      builder: stub.SubscriberStubSettings.Builder
  ): stub.SubscriberStubSettings = builder.build()

  implicit def publisherStubSettingsToInstance(
      builder: stub.PublisherStubSettings.Builder
  ): stub.PublisherStubSettings = builder.build()

  implicit def unaryCallableConversion[REQUEST, RESPONSE](
      callable: UnaryCallable[REQUEST, RESPONSE]
  ): REQUEST => Future[RESPONSE] = request => callable.futureCall(request).asScala
}
