package gcloud.scala.pubsub

import com.google.auth.Credentials
import com.google.protobuf.empty.Empty
import com.google.protobuf.field_mask.FieldMask
import com.google.pubsub.v1._
import io.grpc.Channel
import io.grpc.auth.MoreCallCredentials

import scala.concurrent.{ExecutionContext, Future}

trait PubSubSubscriber extends AutoCloseable {
  implicit val executionContext: ExecutionContext

  def getChannel: Channel
  def getCredentials: Credentials

  private lazy val subscriberStub = SubscriberGrpc
    .stub(getChannel)
    .withCallCredentials(MoreCallCredentials.from(getCredentials))

  def getSubscription(subscriptionName: SubscriptionName): Future[Option[Subscription]] =
    listSubscriptions(subscriptionName.projectName)
      .map(response => response.subscriptions.find(_.name == subscriptionName.fullName))

  def createSubscription(subscription: Subscription): Future[Subscription] =
    subscriberStub
      .createSubscription(subscription)

  def updateSubscription(subscription: Subscription,
                         updateMask: Option[FieldMask] = None): Future[Subscription] =
    subscriberStub.updateSubscription(UpdateSubscriptionRequest(Some(subscription), updateMask))

  def deleteSubscription(subscriptionName: SubscriptionName): Future[Empty] =
    subscriberStub.deleteSubscription(DeleteSubscriptionRequest(subscriptionName.fullName))

  def listSubscriptions(projectName: ProjectName,
                        pageSize: Option[Int] = None,
                        pageToken: Option[String] = None): Future[ListSubscriptionsResponse] =
    subscriberStub.listSubscriptions(
      ListSubscriptionsRequest(projectName.fullName, pageSize.getOrElse(0), pageToken.getOrElse(""))
    )

  def pull(subscriptionName: SubscriptionName,
           returnImmediately: Boolean = false,
           maxMessages: Option[Int] = None): Future[Seq[ReceivedMessage]] =
    subscriberStub
      .pull(PullRequest(subscriptionName.fullName, returnImmediately, maxMessages.getOrElse(0)))
      .map(_.receivedMessages.to[Seq])

  def acknowledge(subscriptionName: SubscriptionName, ackIds: Seq[String]): Future[Empty] =
    subscriberStub.acknowledge(AcknowledgeRequest(subscriptionName.fullName, ackIds))

  def modifyAckDeadline(subscriptionName: SubscriptionName,
                        ackIds: Seq[String],
                        ackDeadlineSeconds: Option[Int] = None): Future[Empty] =
    subscriberStub.modifyAckDeadline(
      ModifyAckDeadlineRequest(subscriptionName.fullName, ackIds, ackDeadlineSeconds.getOrElse(0))
    )

  def modifyPushConfig(subscriptionName: SubscriptionName, pushConfig: PushConfig): Future[Empty] =
    subscriberStub.modifyPushConfig(
      ModifyPushConfigRequest(subscriptionName.fullName, Some(pushConfig))
    )
}
