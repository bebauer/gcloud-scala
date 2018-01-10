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

trait PubSubPublisher extends AutoCloseable {

  implicit val executionContext: ExecutionContext
  implicit val retryScheduler: RetryScheduler

  val listTopicsSettings: CallSettings
  val createTopicSettings: CallSettings
  val updateTopicSettings: CallSettings
  val deleteTopicSettings: CallSettings
  val listTopicSubscriptionsSettings: CallSettings
  val publishSettings: CallSettings

  def getChannel: Channel
  def getCredentials: Credentials

  private lazy val publisherStub = PublisherGrpc
    .stub(getChannel)
    .withCallCredentials(MoreCallCredentials.from(getCredentials))

  def listTopics(projectName: ProjectName,
                 pageSize: Option[Int] = None,
                 pageToken: Option[String] = None): Future[ListTopicsResponse] =
    GrpcCall(listTopicsSettings) {
      publisherStub
        .listTopics(
          ListTopicsRequest(projectName.fullName, pageSize.getOrElse(0), pageToken.getOrElse(""))
        )
    }

  def createTopic(topic: Topic): Future[Topic] =
    GrpcCall(createTopicSettings) {
      publisherStub
        .createTopic(topic)
    }

  def getTopic(topicName: TopicName): Future[Option[Topic]] =
    listTopics(topicName.projectName)
      .map(_.topics)
      .map(topics => topics.find(_.name == topicName.fullName))

  def publish(topicName: TopicName, messages: Seq[PubsubMessage]): Future[Seq[String]] =
    GrpcCall(publishSettings) {
      publisherStub.publish(PublishRequest(topicName.fullName, messages))
    }.map(_.messageIds.to[Seq])

  def updateTopic(topic: Topic, updateMask: Option[FieldMask] = None): Future[Topic] =
    GrpcCall(updateTopicSettings) {
      publisherStub.updateTopic(UpdateTopicRequest(Some(topic), updateMask))
    }

  def deleteTopic(topicName: TopicName): Future[Empty] =
    GrpcCall(deleteTopicSettings) {
      publisherStub.deleteTopic(DeleteTopicRequest(topicName.fullName))
    }

  def listTopicSubscriptions(
      topicName: TopicName,
      pageSize: Option[Int] = None,
      pageToken: Option[String] = None
  ): Future[ListTopicSubscriptionsResponse] =
    GrpcCall(listTopicSubscriptionsSettings) {
      publisherStub.listTopicSubscriptions(
        ListTopicSubscriptionsRequest(topicName.fullName,
                                      pageSize.getOrElse(0),
                                      pageToken.getOrElse(""))
      )
    }
}
