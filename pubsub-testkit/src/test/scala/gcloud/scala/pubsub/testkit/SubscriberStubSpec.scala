package gcloud.scala.pubsub.testkit

import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import gcloud.scala.pubsub._
import gcloud.scala.pubsub.syntax._

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._

class SubscriberStubSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with PubSubTestKit
    with DockerPubSub {

  override implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  "SubscrierStub" should {
    "pull messages" in {
      val settings                       = newTestSetup()
      val (project, topic, subscription) = settings

      publishMessages(settings, "1", "2", "3")

      val stub = SubscriberStub(pubSubUrl)

      val response = Await.result(
        stub.pullAsync(
          PullRequest(subscription = subscription, maxMessages = 10, returnImmediately = true)
        ),
        5.seconds
      )

      response.receivedMessages.map(_.getMessage.getData.toStringUtf8) should contain only ("1", "2", "3")
    }
  }
}
