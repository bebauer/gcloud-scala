package gcloud.scala.pubsub.testkit

import com.google.pubsub.v1
import com.google.pubsub.v1.PushConfig
import gcloud.scala.pubsub.{SubscriptionName, _}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{AsyncWordSpec, Matchers}

import scala.concurrent.ExecutionContextExecutor

class SubscriptionAdminClientSpec
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
      val subscription     = Subscription(subscriptionName, topic)

      whenReady(subscriptionAdminClient.createSubscriptionAsync(subscription)) { result =>
        result.getName shouldBe subscriptionName.fullName
      }
    }

    "get existing subscription" in {
      val (_, _, subscription) = newTestSetup()

      subscriptionAdminClient.getSubscriptionAsync(subscription).map {
        case Some(s) => s.getName shouldBe subscription.fullName
        case None    => fail()
      }
    }

    "get non existing subscription" in {
      val (project, _, _) = newTestSetup()

      subscriptionAdminClient.getSubscriptionAsync(SubscriptionName(project, "doesnotexist")).map {
        case None    => succeed
        case Some(_) => fail()
      }
    }

    "delete subscription" in {
      val (_, _, subscription) = newTestSetup()

      subscriptionAdminClient.deleteSubscriptionAsync(subscription).flatMap { _ =>
        subscriptionAdminClient.getSubscriptionAsync(subscription).map {
          case None    => succeed
          case Some(_) => fail()
        }
      }
    }

    "update subscription" ignore {
      val (_, _, subscription) = newTestSetup()

      subscriptionAdminClient
        .updateSubscriptionAsync(v1.Subscription
                                   .newBuilder()
                                   .setNameWithSubscriptionName(subscription.getSubscription)
                                   .setAckDeadlineSeconds(60)
                                   .build(),
                                 None)
        .flatMap { _ =>
          subscriptionAdminClient.getSubscriptionAsync(subscription).map {
            case Some(s) => s.getAckDeadlineSeconds shouldBe 60
            case None    => fail()
          }
        }
    }

    "list subscriptions" in {
      val (project, _, _) = newTestSetup()

      subscriptionAdminClient.listSubscriptionsAsync(project).map { result =>
        result.subscriptions should have size 1
      }
    }

    "modify push config" in {
      val (_, _, subscription) = newTestSetup()

      subscriptionAdminClient
        .modifyPushConfigAsync(
          subscription,
          PushConfig.newBuilder().setPushEndpoint("http://localhost:9999").build()
        )
        .map(_ => succeed)
    }
  }
}
