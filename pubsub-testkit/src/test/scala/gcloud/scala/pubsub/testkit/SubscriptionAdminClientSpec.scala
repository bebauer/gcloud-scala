package gcloud.scala.pubsub.testkit

import gcloud.scala.pubsub._
import gcloud.scala.pubsub.syntax._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, OptionValues, WordSpec}

import scala.concurrent.ExecutionContextExecutor

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

      val subscriptionName = ProjectSubscriptionName(project, "testSubscription")
      val subscription     = Subscription(subscriptionName, topic)

      whenReady(subscriptionAdminClient.createSubscriptionAsync(subscription)) { result =>
        result.getName shouldBe subscriptionName.fullName
      }
    }

    "get existing subscription" in {
      val (_, _, subscription) = newTestSetup()

      subscriptionAdminClient
        .getSubscriptionOptionAsync(subscription)
        .futureValue
        .value
        .getName shouldBe subscription.fullName
    }

    "get non existing subscription" in {
      val (project, _, _) = newTestSetup()

      subscriptionAdminClient
        .getSubscriptionOptionAsync(ProjectSubscriptionName(project, "doesnotexist"))
        .futureValue shouldBe None
    }

    "delete subscription" in {
      val (_, _, subscription) = newTestSetup()

      whenReady(subscriptionAdminClient.deleteSubscriptionAsync(subscription)) { _ =>
        subscriptionAdminClient.getSubscriptionOptionAsync(subscription).futureValue shouldBe None
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
          .getAckDeadlineSeconds shouldBe 60
      }
    }

    "list subscriptions" in {
      val (project, _, _) = newTestSetup()

      subscriptionAdminClient
        .listSubscriptionsAsync(project = project)
        .futureValue
        .subscriptions should have size 1
    }
  }
}
