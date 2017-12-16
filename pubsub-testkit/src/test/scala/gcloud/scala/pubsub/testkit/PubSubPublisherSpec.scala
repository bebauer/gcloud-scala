package gcloud.scala.pubsub.testkit

import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import gcloud.scala.pubsub.TopicName
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{AsyncWordSpec, Matchers}

import scala.concurrent.ExecutionContextExecutor

class PubSubPublisherSpec extends AsyncWordSpec with Matchers with ScalaFutures with PubSubTestKit {

  override implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  override implicit val patienceConfig =
    PatienceConfig(timeout = Span(60, Seconds), interval = Span(500, Millis))

  "The PubSubPublisher" should {

    "list topics" in {
      val (project, _, _) = newTestSetup()

      client.listTopics(project).map { response =>
        response.topics should have size 1
      }
    }

    "publish string messages" in {
      val (_, topic, _) = newTestSetup()

      client.publish(topic, collection.immutable.Seq("XXX", "AAA")).map { ids =>
        ids should have size 2
      }
    }

    "publish pubsub messages" in {
      val (_, topic, _) = newTestSetup()

      client
        .publish(topic, collection.immutable.Seq(PubsubMessage(ByteString.copyFromUtf8("XXX"))))
        .map { ids =>
          ids should have size 1
        }
    }

    "get existing topic" in {
      val (_, topic, _) = newTestSetup()

      client.getTopic(topic).map {
        case Some(t) => t.name shouldBe topic.fullName
        case None    => fail()
      }
    }

    "get non existing topic" in {
      val (project, _, _) = newTestSetup()

      client.getTopic(TopicName(project, "doesnotexit")).map {
        case Some(_) => fail()
        case None    => succeed
      }
    }

    "delete topic" in {
      val (_, topic, _) = newTestSetup()

      client
        .deleteTopic(topic)
        .flatMap { _ =>
          client.getTopic(topic).map {
            case Some(_) => fail()
            case None    => succeed
          }
        }
    }

    "list topic subscriptions" in {
      val (_, topic, subscription) = newTestSetup()

      client.listTopicSubscriptions(topic).map { response =>
        response.subscriptions should contain(subscription.fullName)
      }
    }
  }
}
