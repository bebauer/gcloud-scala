package gcloud.scala.pubsub.testkit

import gcloud.scala.pubsub._
import gcloud.scala.pubsub.syntax._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, OptionValues, WordSpec}

import scala.concurrent.ExecutionContextExecutor

class TopicAdminClientSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with OptionValues
    with PubSubTestKit
    with DockerPubSub {

  override implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  override implicit val patienceConfig =
    PatienceConfig(timeout = Span(60, Seconds), interval = Span(500, Millis))

  "The TopicAdminClient" should {

    "list topics" in {
      val (project, _, _) = newTestSetup()

      topicAdminClient.listTopicsAsync(project = project).futureValue.topics should have size 1
    }

    "get existing topic" in {
      val (_, topic, _) = newTestSetup()

      topicAdminClient.getTopicOptionAsync(topic).futureValue.value.getName shouldBe topic.fullName
    }

    "get non existing topic" in {
      val (project, _, _) = newTestSetup()

      topicAdminClient.getTopicOptionAsync(ProjectTopicName(project, "doesnotexit")).futureValue shouldBe None
    }

    "delete topic" in {
      val (_, topic, _) = newTestSetup()

      whenReady(topicAdminClient.deleteTopicAsync(topic)) { _ =>
        topicAdminClient.getTopicOptionAsync(topic).futureValue shouldBe None
      }
    }

    "list topic subscriptions" in {
      val (_, topic, subscription) = newTestSetup()

      topicAdminClient.listTopicSubscriptionsAsync(topic = topic).futureValue.subscriptions should contain(
        subscription
      )
    }
  }
}
