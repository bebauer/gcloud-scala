package gcloud.scala.pubsub.testkit

import com.google.api.gax.core.NoCredentialsProvider
import gcloud.scala.pubsub._
import gcloud.scala.pubsub.syntax._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}

class SubscriberStubSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with PubSubTestKit
    with DockerPubSub {

  override implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  "SubscriberStub" should {
    "pull messages" in {
      val settings             = newTestSetup()
      val (_, _, subscription) = settings

      publishMessages(settings, "1", "2", "3")

      val stub = SubscriberStub(pubSubUrl, NoCredentialsProvider.create())

      val response = Await.result(
        stub.pullAsync(
          PullRequest(subscription = subscription, maxMessages = 10, returnImmediately = true)
        ),
        20.seconds
      )

      response.receivedMessages.map(_.getMessage.getData.toStringUtf8) should contain only ("1", "2", "3")
    }
  }
}
