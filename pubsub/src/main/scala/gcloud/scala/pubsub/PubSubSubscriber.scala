package gcloud.scala.pubsub

import com.google.auth.Credentials
import com.google.protobuf.empty.Empty
import com.google.protobuf.field_mask.FieldMask
import com.google.pubsub.v1._
import gcloud.scala.pubsub.PubSubClientConfig.CallSettings
import gcloud.scala.pubsub.retry.RetryScheduler
import io.grpc.Channel
import io.grpc.auth.MoreCallCredentials

import scala.concurrent.{ExecutionContext, Future}

trait PubSubSubscriber extends AutoCloseable {
  implicit val executionContext: ExecutionContext
  implicit val retryScheduler: RetryScheduler

  val createSubscriptionSettings: CallSettings
  val updateSubscriptionSettings: CallSettings
  val listSubscriptionSettings: CallSettings
  val deleteSubscriptionSettings: CallSettings
  val pullSettings: CallSettings
  val acknowledgeSettings: CallSettings
  val modifyAckDeadlineSettings: CallSettings
  val modifyPushConfigSettings: CallSettings

  def getChannel: Channel
  def getCredentials: Credentials

  private lazy val subscriberStub = SubscriberGrpc
    .stub(getChannel)
    .withCallCredentials(MoreCallCredentials.from(getCredentials))

  def getSubscription(subscriptionName: SubscriptionName): Future[Option[Subscription]] =
    listSubscriptions(subscriptionName.projectName)
      .map(response => response.subscriptions.find(_.name == subscriptionName.fullName))

  def createSubscription(subscription: Subscription): Future[Subscription] =
    GrpcCall(createSubscriptionSettings) {
      subscriberStub.createSubscription(subscription)
    }

  def updateSubscription(subscription: Subscription,
                         updateMask: Option[FieldMask] = None): Future[Subscription] =
    GrpcCall(updateSubscriptionSettings) {
      subscriberStub.updateSubscription(UpdateSubscriptionRequest(Some(subscription), updateMask))
    }

  def deleteSubscription(subscriptionName: SubscriptionName): Future[Empty] =
    GrpcCall(deleteSubscriptionSettings) {
      subscriberStub.deleteSubscription(DeleteSubscriptionRequest(subscriptionName.fullName))
    }

  def listSubscriptions(projectName: ProjectName,
                        pageSize: Option[Int] = None,
                        pageToken: Option[String] = None): Future[ListSubscriptionsResponse] =
    GrpcCall(listSubscriptionSettings) {
      subscriberStub.listSubscriptions(
        ListSubscriptionsRequest(projectName.fullName,
                                 pageSize.getOrElse(0),
                                 pageToken.getOrElse(""))
      )
    }

  def pull(subscriptionName: SubscriptionName,
           returnImmediately: Boolean = false,
           maxMessages: Option[Int] = None): Future[Seq[ReceivedMessage]] =
    GrpcCall(pullSettings) {
      subscriberStub
        .pull(PullRequest(subscriptionName.fullName, returnImmediately, maxMessages.getOrElse(0)))
    }.map(_.receivedMessages.to[Seq])

  def acknowledge(subscriptionName: SubscriptionName, ackIds: Seq[String]): Future[Empty] =
    GrpcCall(acknowledgeSettings) {
      subscriberStub.acknowledge(AcknowledgeRequest(subscriptionName.fullName, ackIds))
    }

  def modifyAckDeadline(subscriptionName: SubscriptionName,
                        ackIds: Seq[String],
                        ackDeadlineSeconds: Option[Int] = None): Future[Empty] =
    GrpcCall(modifyAckDeadlineSettings) {
      subscriberStub.modifyAckDeadline(
        ModifyAckDeadlineRequest(subscriptionName.fullName, ackIds, ackDeadlineSeconds.getOrElse(0))
      )
    }

  def modifyPushConfig(subscriptionName: SubscriptionName, pushConfig: PushConfig): Future[Empty] =
    GrpcCall(modifyPushConfigSettings) {
      subscriberStub.modifyPushConfig(
        ModifyPushConfigRequest(subscriptionName.fullName, Some(pushConfig))
      )
    }
}
