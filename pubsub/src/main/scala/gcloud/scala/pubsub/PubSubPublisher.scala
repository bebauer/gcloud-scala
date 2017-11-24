package gcloud.scala.pubsub

import com.google.auth.Credentials
import com.google.protobuf.empty.Empty
import com.google.protobuf.field_mask.FieldMask
import com.google.pubsub.v1._
import io.grpc.Channel
import io.grpc.auth.MoreCallCredentials

import scala.concurrent.{ExecutionContext, Future}

trait PubSubPublisher extends AutoCloseable {

  implicit val executionContext: ExecutionContext

  def getChannel: Channel
  def getCredentials: Credentials

  private lazy val publisherStub = PublisherGrpc
    .stub(getChannel)
    .withCallCredentials(MoreCallCredentials.from(getCredentials))

  def listTopics(projectName: ProjectName,
                 pageSize: Option[Int] = None,
                 pageToken: Option[String] = None): Future[ListTopicsResponse] =
    publisherStub
      .listTopics(
        ListTopicsRequest(projectName.fullName, pageSize.getOrElse(0), pageToken.getOrElse(""))
      )

  def createTopic(topic: Topic): Future[Topic] =
    publisherStub
      .createTopic(topic)

  def getTopic(topicName: TopicName): Future[Option[Topic]] =
    listTopics(topicName.projectName)
      .map(_.topics)
      .map(topics => topics.find(_.name == topicName.fullName))

  def publish(topicName: TopicName, messages: Seq[PubsubMessage]): Future[Seq[String]] =
    publisherStub.publish(PublishRequest(topicName.fullName, messages)).map(_.messageIds.to[Seq])

  def updateTopic(topic: Topic, updateMask: Option[FieldMask] = None): Future[Topic] =
    publisherStub.updateTopic(UpdateTopicRequest(Some(topic), updateMask))

  def deleteTopic(topicName: TopicName): Future[Empty] =
    publisherStub.deleteTopic(DeleteTopicRequest(topicName.fullName))

  def listTopicSubscriptions(
      topicName: TopicName,
      pageSize: Option[Int] = None,
      pageToken: Option[String] = None
  ): Future[ListTopicSubscriptionsResponse] =
    publisherStub.listTopicSubscriptions(
      ListTopicSubscriptionsRequest(topicName.fullName,
                                    pageSize.getOrElse(0),
                                    pageToken.getOrElse(""))
    )
}
