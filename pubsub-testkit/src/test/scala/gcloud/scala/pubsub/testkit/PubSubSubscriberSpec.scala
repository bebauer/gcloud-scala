package gcloud.scala.pubsub.testkit

import com.google.protobuf.ByteString
import com.google.protobuf.field_mask.FieldMask
import com.google.pubsub.v1.{PubsubMessage, PushConfig, Subscription}
import gcloud.scala.pubsub.SubscriptionName
import io.grpc.StatusRuntimeException
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{AsyncWordSpec, Matchers}

import scala.concurrent.{ExecutionContextExecutor, Future}

class PubSubSubscriberSpec
    extends AsyncWordSpec
    with Matchers
    with ScalaFutures
    with PubSubTestKit {

  override implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  override implicit val patienceConfig =
    PatienceConfig(timeout = Span(60, Seconds), interval = Span(500, Millis))

  "The PubSubSubscriber" should {

    "create a subscription" in {
      val (project, topic, _) = newTestSetup()

      val subscriptionName = SubscriptionName(project, "testSubscription")
      val subscription     = Subscription(subscriptionName.fullName, topic.fullName)

      whenReady(client.createSubscription(subscription)) { result =>
        result.name shouldBe subscriptionName.fullName
      }
    }

    "get existing subscription" in {
      val (_, _, subscription) = newTestSetup()

      client.getSubscription(subscription).map {
        case Some(s) => s.name shouldBe subscription.fullName
        case None    => fail()
      }
    }

    "get non existing subscription" in {
      val (project, _, _) = newTestSetup()

      client.getSubscription(SubscriptionName(project, "doesnotexist")).map {
        case None    => succeed
        case Some(_) => fail()
      }
    }

    "delete subscription" in {
      val (_, _, subscription) = newTestSetup()

      client.deleteSubscription(subscription).flatMap { _ =>
        client.getSubscription(subscription).map {
          case None    => succeed
          case Some(_) => fail()
        }
      }
    }

    "update subscription" ignore {
      val (_, _, subscription) = newTestSetup()

      client
        .updateSubscription(Subscription(subscription.fullName, ackDeadlineSeconds = 60),
                            Some(FieldMask(Seq("ackDeadlineSeconds"))))
        .flatMap { _ =>
          client.getSubscription(subscription).map {
            case Some(s) => s.ackDeadlineSeconds shouldBe 60
            case None    => fail()
          }
        }
    }

    "list subscriptions" in {
      val (project, _, _) = newTestSetup()

      client.listSubscriptions(project.fullName).map { result =>
        result.subscriptions should have size 1
      }
    }

    "pull messages" in {
      val settings             = newTestSetup()
      val (_, _, subscription) = settings

      publishMessages(settings, "A", "B")
      publishMessages(settings, PubsubMessage(ByteString.copyFromUtf8("C")))

      whenReady(
        client.pull(subscription, returnImmediately = true, maxMessages = Some(10))
      ) { result =>
        result.size shouldBe 3
      }
    }

    "pull fails with invalid subscription" in {
      val (project, _, _)  = newTestSetup()
      val subscriptionName = SubscriptionName(project, "testSubscription")

      recoverToSucceededIf[StatusRuntimeException] {
        client.pull(
          subscriptionName,
          returnImmediately = true,
          maxMessages = Some(10)
        )
      }
    }

    "acknowledge messages" in {
      val settings             = newTestSetup()
      val (_, _, subscription) = settings

      publishMessages(settings, "A")

      client
        .pull(subscription, returnImmediately = true, maxMessages = Some(10))
        .flatMap { response =>
          response.size shouldBe 1

          Future.sequence(response.map { message =>
            client.acknowledge(
              subscription,
              collection.immutable.Seq(message.ackId)
            )
          })
        }
        .map(_ => succeed)
    }

    "modify ack deadline" in {
      val settings             = newTestSetup()
      val (_, _, subscription) = settings

      publishMessages(settings, "A")

      client
        .pull(subscription, returnImmediately = true, maxMessages = Some(10))
        .flatMap { messages =>
          messages.size shouldBe 1

          client.modifyAckDeadline(subscription, messages.map(_.ackId), Some(60))
        }
        .map(_ => succeed)
    }

    "modify push config" in {
      val (_, _, subscription) = newTestSetup()

      client
        .modifyPushConfig(subscription, PushConfig("http://localhost:9999"))
        .map(_ => succeed)
    }
  }
}
