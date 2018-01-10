package gcloud.scala.pubsub

import com.google.auth.Credentials
import com.google.protobuf.empty.Empty
import com.google.protobuf.field_mask.FieldMask
import com.google.pubsub.v1._
import gcloud.scala.pubsub.retry.RetryScheduler
import io.grpc.Channel
import io.grpc.auth.MoreCallCredentials

import scala.concurrent.{ExecutionContext, Future}

trait PubSubSubscriber extends AutoCloseable {
  implicit val executionContext: ExecutionContext
  implicit val retryScheduler: RetryScheduler

  def getChannel: Channel
  def getCredentials: Credentials

  private lazy val subscriberStub = SubscriberGrpc
    .stub(getChannel)
    .withCallCredentials(MoreCallCredentials.from(getCredentials))

  def getSubscription(
      subscriptionName: SubscriptionName
  )(implicit callSettings: CallSettings[ListSubscriptionsRequest]): Future[Option[Subscription]] =
    listSubscriptions(subscriptionName.projectName)(callSettings)
      .map(response => response.subscriptions.find(_.name == subscriptionName.fullName))

  def createSubscription(
      subscription: Subscription
  )(implicit callSettings: CallSettings[Subscription]): Future[Subscription] =
    GrpcCall(subscriberStub.createSubscription(subscription), callSettings)

  def updateSubscription(subscription: Subscription, updateMask: Option[FieldMask] = None)(
      implicit callSettings: CallSettings[UpdateSubscriptionRequest]
  ): Future[Subscription] =
    GrpcCall(
      subscriberStub.updateSubscription(UpdateSubscriptionRequest(Some(subscription), updateMask)),
      callSettings
    )

  def deleteSubscription(
      subscriptionName: SubscriptionName
  )(implicit callSettings: CallSettings[DeleteSubscriptionRequest]): Future[Empty] =
    GrpcCall(
      subscriberStub.deleteSubscription(DeleteSubscriptionRequest(subscriptionName.fullName)),
      callSettings
    )

  def listSubscriptions(projectName: ProjectName,
                        pageSize: Option[Int] = None,
                        pageToken: Option[String] = None)(
      implicit callSettings: CallSettings[ListSubscriptionsRequest]
  ): Future[ListSubscriptionsResponse] =
    GrpcCall(subscriberStub.listSubscriptions(
               ListSubscriptionsRequest(projectName.fullName,
                                        pageSize.getOrElse(0),
                                        pageToken.getOrElse(""))
             ),
             callSettings)

  def pull(
      subscriptionName: SubscriptionName,
      returnImmediately: Boolean = false,
      maxMessages: Option[Int] = None
  )(implicit callSettings: CallSettings[PullRequest]): Future[Seq[ReceivedMessage]] =
    GrpcCall(subscriberStub.pull(
               PullRequest(subscriptionName.fullName, returnImmediately, maxMessages.getOrElse(0))
             ),
             callSettings)
      .map(_.receivedMessages.to[Seq])

  def acknowledge(subscriptionName: SubscriptionName, ackIds: Seq[String])(
      implicit callSettings: CallSettings[AcknowledgeRequest]
  ): Future[Empty] =
    GrpcCall(subscriberStub.acknowledge(AcknowledgeRequest(subscriptionName.fullName, ackIds)),
             callSettings)

  def modifyAckDeadline(
      subscriptionName: SubscriptionName,
      ackIds: Seq[String],
      ackDeadlineSeconds: Option[Int] = None
  )(implicit callSettings: CallSettings[ModifyAckDeadlineRequest]): Future[Empty] =
    GrpcCall(
      subscriberStub.modifyAckDeadline(
        ModifyAckDeadlineRequest(subscriptionName.fullName, ackIds, ackDeadlineSeconds.getOrElse(0))
      ),
      callSettings
    )

  def modifyPushConfig(subscriptionName: SubscriptionName, pushConfig: PushConfig)(
      implicit callSettings: CallSettings[ModifyPushConfigRequest]
  ): Future[Empty] =
    GrpcCall(subscriberStub.modifyPushConfig(
               ModifyPushConfigRequest(subscriptionName.fullName, Some(pushConfig))
             ),
             callSettings)

}
