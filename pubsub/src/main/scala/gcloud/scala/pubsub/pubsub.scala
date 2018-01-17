package gcloud.scala

import com.google.cloud
import com.google.cloud.pubsub.v1._
import com.google.protobuf.{Empty, FieldMask}
import com.google.pubsub.v1
import com.google.pubsub.v1.{ProjectName, _}
import org.threeten.bp.Duration

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

package object pubsub {
  import gcloud.scala.pubsub.FutureConversions.Implicits._

  /**
    * Implicitly converts a string to a [[ProjectName]] by calling [[ProjectName.apply()]].
    *
    * @param name the name
    * @return the [[ProjectName]]
    */
  implicit def projectFromString(name: String): ProjectName = ProjectName(name)

  object ProjectName {
    private val ProjectNamePattern = "(projects/)?(.+)".r

    /**
      * Creates a [[ProjectName]] by parsing a name string.
      * The name can be specified with or without the 'projects/' prefix.
      *
      * @param name the project name
      * @return the [[ProjectName]]
      * @throws IllegalArgumentException if the name cannot be parsed
      */
    def apply(name: String): ProjectName =
      v1.ProjectName
        .newBuilder()
        .setProject(name match {
          case ProjectNamePattern(n)    => n
          case ProjectNamePattern(_, n) => n
          case _ =>
            throw new IllegalArgumentException(
              s"Project name '$name' does not match pattern '$ProjectNamePattern'."
            )
        })
        .build()
  }

  /**
    * Implicitly converts a string to a [[TopicName]] by calling [[TopicName.apply()]].
    *
    * @param fullName the full name
    * @return the [[TopicName]]
    */
  implicit def topicFromString(fullName: String): TopicName = TopicName(fullName)

  object TopicName {
    private val TopicNamePattern = "projects/(.+)/topics/(.+)".r

    /**
      * Creates a [[TopicName]] by parsing the full name (projects/{project}/topics/{topic}) string.
      *
      * @param fullName the full name string
      * @return the [[TopicName]]
      * @throws IllegalArgumentException if the full name cannot be parsed
      */
    def apply(fullName: String): TopicName = fullName match {
      case TopicNamePattern(project, topic) => TopicName(project, topic)
      case _ =>
        throw new IllegalArgumentException(
          s"Full topic name '$fullName' does not match pattern '$TopicNamePattern'."
        )
    }

    /**
      * Creates a [[TopicName]] from a [[ProjectName]] and a topic name.
      *
      * @param projectName the [[ProjectName]]
      * @param name the topic name
      * @return the [[TopicName]]
      */
    def apply(projectName: ProjectName, name: String): TopicName =
      v1.TopicName.newBuilder().setProject(projectName.getProject).setTopic(name).build()
  }

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

  object SubscriptionName {
    private val SubscriptionNamePattern = "projects/(.+)/subscriptions/(.+)".r

    /**
      * Creates a [[SubscriptionName]] by parsing the full name
      * (projects/{project}/subscriptions/{subscription}) string.
      *
      * @param fullName the full name string
      * @return the [[SubscriptionName]]
      * @throws IllegalArgumentException if the full name cannot be parsed
      */
    def apply(fullName: String): SubscriptionName = fullName match {
      case SubscriptionNamePattern(project, subscription) =>
        SubscriptionName(project, subscription)
      case _ =>
        throw new IllegalArgumentException(
          s"Full subscription name '$fullName' does not match pattern '$SubscriptionNamePattern'."
        )
    }

    /**
      * Creates a [[SubscriptionName]] from a [[ProjectName]] and a subscription name.
      *
      * @param projectName the [[ProjectName]]
      * @param name the subscription name
      * @return the [[SubscriptionName]]
      */
    def apply(projectName: ProjectName, name: String): SubscriptionName =
      v1.SubscriptionName
        .newBuilder()
        .setProject(projectName.getProject)
        .setSubscription(name)
        .build()
  }

  object Topic {
    def apply(topicName: TopicName): Topic =
      v1.Topic.newBuilder().setNameWithTopicName(topicName).build()
  }

  object Subscription {
    def apply(subscriptionName: SubscriptionName, topicName: TopicName): Subscription =
      v1.Subscription
        .newBuilder()
        .setTopic(topicName.fullName)
        .setNameWithSubscriptionName(subscriptionName)
        .build()
  }

  object TopicAdminClient {
    def apply(pubSubUrl: PubSubUrl): TopicAdminClient =
      cloud.pubsub.v1.TopicAdminClient
        .create(TopicAdminSettings.newBuilder().setEndpoint(pubSubUrl.url).build())

    def apply(pubSubUrl: PubSubUrl, maxInboundMessageSize: Int): TopicAdminClient =
      cloud.pubsub.v1.TopicAdminClient
        .create(
          TopicAdminSettings
            .newBuilder()
            .setTransportChannelProvider(
              TopicAdminSettings
                .defaultGrpcTransportProviderBuilder()
                .setEndpoint(pubSubUrl.url)
                .setMaxInboundMessageSize(maxInboundMessageSize)
                .build()
            )
            .build()
        )

    def apply(): TopicAdminClient = cloud.pubsub.v1.TopicAdminClient.create()
  }

  object SubscriptionAdminClient {
    def apply(pubSubUrl: PubSubUrl): SubscriptionAdminClient =
      cloud.pubsub.v1.SubscriptionAdminClient
        .create(SubscriptionAdminSettings.newBuilder().setEndpoint(pubSubUrl.url).build())

    def apply(pubSubUrl: PubSubUrl, maxInboundMessageSize: Int): SubscriptionAdminClient =
      cloud.pubsub.v1.SubscriptionAdminClient
        .create(
          SubscriptionAdminSettings
            .newBuilder()
            .setTransportChannelProvider(
              SubscriptionAdminSettings
                .defaultGrpcTransportProviderBuilder()
                .setEndpoint(pubSubUrl.url)
                .setMaxInboundMessageSize(maxInboundMessageSize)
                .build()
            )
            .build()
        )

    def apply(): TopicAdminClient = cloud.pubsub.v1.TopicAdminClient.create()
  }

  object Subscriber {
    private final val MaxInboundMessageSize = 20 * 1024 * 1024 // 20MB API maximum message size.

    def apply(subscriptionName: SubscriptionName,
              messageReceiver: MessageReceiver,
              pubSubUrl: PubSubUrl = PubSubUrl.DefaultPubSubUrl,
              maxInboundMessageSize: Int = MaxInboundMessageSize): Subscriber =
      cloud.pubsub.v1.Subscriber
        .newBuilder(subscriptionName, messageReceiver)
        .setChannelProvider(
          SubscriptionAdminSettings.defaultGrpcTransportProviderBuilder
            .setEndpoint(pubSubUrl.url)
            .setMaxInboundMessageSize(maxInboundMessageSize)
            .setKeepAliveTime(Duration.ofMinutes(5))
            .build()
        )
        .build()
  }

  implicit class SubscriptionNameExtensions(val subscriptionName: SubscriptionName) extends AnyVal {
    def fullName: String = subscriptionName.toString
  }

  implicit class SubscriptionAdminClientValueClass(val client: SubscriptionAdminClient)
      extends AnyVal {
    def getSubscriptionAsync(subscriptionName: SubscriptionName): Future[Option[Subscription]] = {
      implicit val ec =
        ExecutionContext.fromExecutor(client.getSettings.getExecutorProvider.getExecutor)

      listSubscriptionsAsync(subscriptionName.getProject)
        .map(response => response.getSubscriptionsList.asScala)
        .map(_.find(_.getName == subscriptionName.fullName))
    }

    def createSubscriptionAsync(subscription: Subscription): Future[Subscription] =
      client.createSubscriptionCallable().futureCall(subscription)

    def updateSubscriptionAsync(subscription: Subscription,
                                updateMask: Option[FieldMask] = None): Future[Subscription] = {
      val requestBuilder = UpdateSubscriptionRequest
        .newBuilder()
        .setSubscription(subscription)

      updateMask.foreach(requestBuilder.setUpdateMask)

      client
        .updateSubscriptionCallable()
        .futureCall(
          requestBuilder.build()
        )
    }

    def deleteSubscriptionAsync(subscriptionName: SubscriptionName): Future[Empty] =
      client
        .deleteSubscriptionCallable()
        .futureCall(
          DeleteSubscriptionRequest
            .newBuilder()
            .setSubscriptionWithSubscriptionName(subscriptionName)
            .build()
        )

    def listSubscriptionsAsync(
        projectName: ProjectName,
        pageSize: Option[Int] = None,
        pageToken: Option[String] = None
    ): Future[ListSubscriptionsResponse] =
      client
        .listSubscriptionsCallable()
        .futureCall(
          ListSubscriptionsRequest
            .newBuilder()
            .setProjectWithProjectName(projectName)
            .setPageSize(pageSize.getOrElse(0))
            .setPageToken(pageToken.getOrElse(""))
            .build()
        )

    def modifyPushConfigAsync(subscriptionName: SubscriptionName,
                              pushConfig: PushConfig): Future[Empty] =
      client
        .modifyPushConfigCallable()
        .futureCall(ModifyPushConfigRequest.newBuilder().setPushConfig(pushConfig).build())
  }

  implicit class TopicAdminClientValueClass(val client: TopicAdminClient) extends AnyVal {
    def listTopicsAsync(
        projectName: ProjectName,
        pageSize: Option[Int] = None,
        pageToken: Option[String] = None
    ): Future[ListTopicsResponse] =
      client
        .listTopicsCallable()
        .futureCall(
          ListTopicsRequest
            .newBuilder()
            .setProjectWithProjectName(projectName)
            .setPageSize(pageSize.getOrElse(0))
            .setPageToken(pageToken.getOrElse(""))
            .build()
        )

    def createTopicAsync(topic: Topic): Future[Topic] =
      client.createTopicCallable().futureCall(topic)

    def getTopicAsync(topicName: TopicName): Future[Option[Topic]] = {
      implicit val ec =
        ExecutionContext.fromExecutor(client.getSettings.getExecutorProvider.getExecutor)

      listTopicsAsync(topicName.getProject)
        .map(_.getTopicsList.asScala)
        .map(_.find(_.getName == topicName.fullName))
    }

    def deleteTopicAsync(topicName: TopicName): Future[Empty] =
      client
        .deleteTopicCallable()
        .futureCall(DeleteTopicRequest.newBuilder().setTopicWithTopicName(topicName).build())

    def listTopicSubscriptionsAsync(
        topicName: TopicName,
        pageSize: Option[Int] = None,
        pageToken: Option[String] = None
    ): Future[ListTopicSubscriptionsResponse] =
      client
        .listTopicSubscriptionsCallable()
        .futureCall(
          ListTopicSubscriptionsRequest
            .newBuilder()
            .setTopicWithTopicName(topicName)
            .setPageSize(pageSize.getOrElse(0))
            .setPageToken(pageToken.getOrElse(""))
            .build()
        )
  }

  implicit class ListTopicResponseExtensions(val listTopicsResponse: ListTopicsResponse)
      extends AnyVal {
    def topics: Seq[Topic] = listTopicsResponse.getTopicsList.asScala
  }

  implicit class ListTopicSubscriptionsResponseExtensions(
      val listTopicSubscriptionsResponse: ListTopicSubscriptionsResponse
  ) extends AnyVal {
    def subscriptions: Seq[SubscriptionName] =
      listTopicSubscriptionsResponse.getSubscriptionsListAsSubscriptionNameList.asScala
  }

  implicit class ListSubscriptionsResponseExtensions(
      val listSubscriptionsResponse: ListSubscriptionsResponse
  ) extends AnyVal {
    def subscriptions: Seq[Subscription] = listSubscriptionsResponse.getSubscriptionsList.asScala
  }
}
