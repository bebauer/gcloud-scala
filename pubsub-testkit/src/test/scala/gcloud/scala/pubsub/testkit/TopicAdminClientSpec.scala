package gcloud.scala.pubsub.testkit

import gcloud.scala.pubsub.{TopicName, _}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{AsyncWordSpec, Matchers}

import scala.concurrent.ExecutionContextExecutor

class TopicAdminClientSpec
    extends AsyncWordSpec
    with Matchers
    with ScalaFutures
    with PubSubTestKit {

  override implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  override implicit val patienceConfig =
    PatienceConfig(timeout = Span(60, Seconds), interval = Span(500, Millis))

  "The PubSubPublisher" should {

    "list topics" in {
      val (project, _, _) = newTestSetup()

      topicAdminClient.listTopicsAsync(project).map { response =>
        response.topics should have size 1
      }
    }

    "get existing topic" in {
      val (_, topic, _) = newTestSetup()

      topicAdminClient.getTopicAsync(topic).map {
        case Some(t) => t.getName shouldBe topic.fullName
        case None    => fail()
      }
    }

    "get non existing topic" in {
      val (project, _, _) = newTestSetup()

      topicAdminClient.getTopicAsync(TopicName(project, "doesnotexit")).map {
        case Some(_) => fail()
        case None    => succeed
      }
    }

    "delete topic" in {
      val (_, topic, _) = newTestSetup()

      topicAdminClient
        .deleteTopicAsync(topic)
        .flatMap { _ =>
          topicAdminClient.getTopicAsync(topic).map {
            case Some(_) => fail()
            case None    => succeed
          }
        }
    }

    "list topic subscriptions" in {
      val (_, topic, subscription) = newTestSetup()

      topicAdminClient.listTopicSubscriptionsAsync(topic).map { response =>
        response.subscriptions should contain(subscription)
      }
    }
  }
}
