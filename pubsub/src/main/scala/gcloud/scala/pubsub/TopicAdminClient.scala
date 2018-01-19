package gcloud.scala.pubsub

import com.google.cloud.pubsub.v1
import com.google.protobuf.Empty
import com.google.pubsub.v1._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

object TopicAdminClient {
  def apply(pubSubUrl: PubSubUrl, maxInboundMessageSize: Option[Int] = None): v1.TopicAdminClient =
    TopicAdminClient(TopicAdminSettings(pubSubUrl, maxInboundMessageSize))

  def apply(): v1.TopicAdminClient = v1.TopicAdminClient.create()

  def apply(settings: v1.TopicAdminSettings): v1.TopicAdminClient =
    v1.TopicAdminClient.create(settings)

  private[pubsub] object Logic {
    import FutureConversions.Implicits._
    import com.google.cloud.pubsub.v1.TopicAdminClient

    def listTopicsAsync(client: TopicAdminClient,
                        projectName: ProjectName,
                        pageSize: Option[Int] = None,
                        pageToken: Option[String] = None): Future[ListTopicsResponse] =
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

    def createTopicAsync(client: TopicAdminClient, topic: Topic): Future[Topic] =
      client.createTopicCallable().futureCall(topic)

    def getTopicAsync(client: TopicAdminClient, topicName: TopicName): Future[Option[Topic]] = {
      implicit val ec =
        ExecutionContext.fromExecutor(client.getSettings.getExecutorProvider.getExecutor)

      listTopicsAsync(client, topicName.getProject)
        .map(_.getTopicsList.asScala)
        .map(_.find(_.getName == topicName.fullName))
    }

    def deleteTopicAsync(client: TopicAdminClient, topicName: TopicName): Future[Empty] =
      client
        .deleteTopicCallable()
        .futureCall(DeleteTopicRequest.newBuilder().setTopicWithTopicName(topicName).build())

    def listTopicSubscriptionsAsync(
        client: TopicAdminClient,
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

}
