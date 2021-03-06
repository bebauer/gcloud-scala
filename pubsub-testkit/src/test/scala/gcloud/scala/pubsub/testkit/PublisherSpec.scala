package gcloud.scala.pubsub.testkit

import com.google.api.gax.core.NoCredentialsProvider
import gcloud.scala.pubsub._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContextExecutor

class PublisherSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with PubSubTestKit
    with DockerPubSub {

  override implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  "Publisher" should {
    "publish messages" in {
      val settings      = newTestSetup()
      val (_, topic, _) = settings

      val publisher = Publisher(topic, pubSubUrl, NoCredentialsProvider.create())

      try {
        publisher.publishAsync("Test1")
        publisher.publishAsync(PubsubMessage("Test2"))

        pullMessages(settings, 2).map(_.getData.toStringUtf8) should contain only ("Test1", "Test2")
      } finally {
        publisher.shutdown()
      }
    }
  }
}
