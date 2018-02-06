package gcloud.scala.pubsub.testkit

import com.google.pubsub.v1.PushConfig
import gcloud.scala.pubsub.{SubscriptionName, _}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, OptionValues, WordSpec}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class SubscriptionAdminClientSpec
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

  "The SubscriptionAdminClient" should {

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

      subscriptionAdminClient
        .getSubscriptionAsync(subscription)
        .futureValue
        .value
        .getName shouldBe subscription.fullName
    }

    "get non existing subscription" in {
      val (project, _, _) = newTestSetup()

      subscriptionAdminClient
        .getSubscriptionAsync(SubscriptionName(project, "doesnotexist"))
        .futureValue shouldBe None
    }

    "delete subscription" in {
      val (_, _, subscription) = newTestSetup()

      whenReady(subscriptionAdminClient.deleteSubscriptionAsync(subscription)) { _ =>
        subscriptionAdminClient.getSubscriptionAsync(subscription).futureValue shouldBe None
      }
    }

    "update subscription" ignore {
      val (_, _, subscription) = newTestSetup()

      whenReady(
        subscriptionAdminClient
          .updateSubscriptionAsync(
            Subscription(subscription.getSubscription)
              .setAckDeadlineSeconds(60)
          )
      ) { _ =>
        subscriptionAdminClient
          .getSubscriptionAsync(subscription)
          .futureValue
          .value
          .getAckDeadlineSeconds shouldBe 60
      }
    }

    "list subscriptions" in {
      val (project, _, _) = newTestSetup()

      subscriptionAdminClient
        .listSubscriptionsAsync(project)
        .futureValue
        .subscriptions should have size 1
    }

    "modify push config" in {
      val (_, _, subscription) = newTestSetup()

      subscriptionAdminClient
        .modifyPushConfigAsync(
          subscription,
          PushConfig.newBuilder().setPushEndpoint("http://localhost:9999").build()
        )
        .isReadyWithin(5.seconds)
    }
  }
}
