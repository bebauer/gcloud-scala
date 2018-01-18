package gcloud.scala.pubsub.testkit

import gcloud.scala.pubsub._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._

class PublisherSpec extends WordSpec with Matchers with ScalaFutures with PubSubTestKit {

  override implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  "Publisher" should {
    "publish messages" in {
      val settings      = newTestSetup()
      val (_, topic, _) = settings

      val publisher = Publisher(topic, pubSubUrl)

      try {
        Await.result(publisher.publishAsync("Test"), 5.seconds)

        pullMessages(settings).map(_.getData.toStringUtf8) shouldEqual Seq("Test")
      } finally {
        publisher.shutdown()
      }
    }
  }
}
