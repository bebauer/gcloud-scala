package gcloud.scala.pubsub

import com.google.cloud.pubsub.v1
import com.google.protobuf.{Empty, FieldMask}
import com.google.pubsub.v1._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

object SubscriptionAdminClient {
  def apply(pubSubUrl: PubSubUrl,
            maxInboundMessageSize: Option[Int] = None): v1.SubscriptionAdminClient =
    SubscriptionAdminClient(SubscriptionAdminSettings(pubSubUrl, maxInboundMessageSize))

  def apply(): v1.SubscriptionAdminClient = v1.SubscriptionAdminClient.create()

  def apply(subscriptionAdminSettings: v1.SubscriptionAdminSettings): v1.SubscriptionAdminClient =
    v1.SubscriptionAdminClient.create(subscriptionAdminSettings)

  private[pubsub] object Logic {
    import FutureConversions.Implicits._
    import com.google.cloud.pubsub.v1.SubscriptionAdminClient

    def getSubscriptionAsync(client: SubscriptionAdminClient,
                             subscriptionName: SubscriptionName): Future[Option[Subscription]] = {
      implicit val ec =
        ExecutionContext.fromExecutor(client.getSettings.getExecutorProvider.getExecutor)

      listSubscriptionsAsync(client, subscriptionName.getProject)
        .map(response => response.getSubscriptionsList.asScala)
        .map(_.find(_.getName == subscriptionName.fullName))
    }

    def createSubscriptionAsync(client: SubscriptionAdminClient,
                                subscription: Subscription): Future[Subscription] =
      client.createSubscriptionCallable().futureCall(subscription)

    def updateSubscriptionAsync(client: SubscriptionAdminClient,
                                subscription: Subscription,
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

    def deleteSubscriptionAsync(client: SubscriptionAdminClient,
                                subscriptionName: SubscriptionName): Future[Empty] =
      client
        .deleteSubscriptionCallable()
        .futureCall(
          DeleteSubscriptionRequest
            .newBuilder()
            .setSubscription(subscriptionName.toString)
            .build()
        )

    def listSubscriptionsAsync(
        client: SubscriptionAdminClient,
        projectName: ProjectName,
        pageSize: Option[Int] = None,
        pageToken: Option[String] = None
    ): Future[ListSubscriptionsResponse] =
      client
        .listSubscriptionsCallable()
        .futureCall(
          ListSubscriptionsRequest
            .newBuilder()
            .setProject(projectName.toString)
            .setPageSize(pageSize.getOrElse(0))
            .setPageToken(pageToken.getOrElse(""))
            .build()
        )

    def modifyPushConfigAsync(client: SubscriptionAdminClient,
                              subscriptionName: SubscriptionName,
                              pushConfig: PushConfig): Future[Empty] =
      client
        .modifyPushConfigCallable()
        .futureCall(
          ModifyPushConfigRequest
            .newBuilder()
            .setSubscription(subscriptionName.toString)
            .setPushConfig(pushConfig)
            .build()
        )
  }
}
