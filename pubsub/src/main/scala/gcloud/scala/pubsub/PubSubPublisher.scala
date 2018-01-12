package gcloud.scala.pubsub

import com.google.auth.Credentials
import com.google.protobuf.empty.Empty
import com.google.protobuf.field_mask.FieldMask
import com.google.pubsub.v1._
import gcloud.scala.pubsub.retry.RetryScheduler
import io.grpc.Channel
import io.grpc.auth.MoreCallCredentials

import scala.concurrent.{ExecutionContext, Future}

trait PubSubPublisher extends AutoCloseable {

  implicit val executionContext: ExecutionContext
  implicit val retryScheduler: RetryScheduler

  def getChannel: Channel
  def getCredentials: Credentials

  private lazy val publisherStub = PublisherGrpc
    .stub(getChannel)
    .withCallCredentials(MoreCallCredentials.from(getCredentials))

  def listTopics(
      projectName: ProjectName,
      pageSize: Option[Int] = None,
      pageToken: Option[String] = None
  )(implicit callSettings: CallSettings[ListTopicsRequest]): Future[ListTopicsResponse] =
    GrpcCall(
      publisherStub.listTopics(
        ListTopicsRequest(projectName.fullName, pageSize.getOrElse(0), pageToken.getOrElse(""))
      ),
      callSettings
    )

  def createTopic(topic: Topic)(implicit callSettings: CallSettings[Topic]): Future[Topic] =
    GrpcCall(publisherStub.createTopic(topic), callSettings)

  def getTopic(
      topicName: TopicName
  )(implicit callSettings: CallSettings[ListTopicsRequest]): Future[Option[Topic]] =
    listTopics(topicName.projectName)(callSettings)
      .map(_.topics)
      .map(topics => topics.find(_.name == topicName.fullName))

  def publish(topicName: TopicName, messages: Seq[PubsubMessage])(
      implicit callSettings: CallSettings[PublishRequest]
  ): Future[Seq[String]] =
    GrpcCall(publisherStub.publish(PublishRequest(topicName.fullName, messages)), callSettings)
      .map(_.messageIds.to[Seq])

  def updateTopic(topic: Topic, updateMask: Option[FieldMask] = None)(
      implicit callSettings: CallSettings[UpdateTopicRequest]
  ): Future[Topic] =
    GrpcCall(publisherStub.updateTopic(UpdateTopicRequest(Some(topic), updateMask)), callSettings)

  def deleteTopic(
      topicName: TopicName
  )(implicit callSettings: CallSettings[DeleteTopicRequest]): Future[Empty] =
    GrpcCall(publisherStub.deleteTopic(DeleteTopicRequest(topicName.fullName)), callSettings)

  def listTopicSubscriptions(
      topicName: TopicName,
      pageSize: Option[Int] = None,
      pageToken: Option[String] = None
  )(
      implicit callSettings: CallSettings[ListTopicSubscriptionsRequest]
  ): Future[ListTopicSubscriptionsResponse] =
    GrpcCall(
      publisherStub.listTopicSubscriptions(
        ListTopicSubscriptionsRequest(topicName.fullName,
                                      pageSize.getOrElse(0),
                                      pageToken.getOrElse(""))
      ),
      callSettings
    )
}
