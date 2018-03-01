package gcloud.scala.pubsub.testkit

import gcloud.scala.pubsub.PublisherStub.PublishRequest
import gcloud.scala.pubsub._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._

class PublisherStubSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with PubSubTestKit
    with DockerPubSub {

  override implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  "PublisherStub" should {
    "publish messages" in {
      val settings      = newTestSetup()
      val (_, topic, _) = settings

      val stub = stubs.PublisherStub(pubSubUrl)

      try {
        Await.ready(stub.publishCallable()(PublishRequest(topic, "Test1")), 5.seconds)
        Await.ready(stub.publishCallable()(PublishRequest(topic, PubSubMessage("Test2"))),
                    5.seconds)

        pullMessages(settings, 2).map(_.getData.toStringUtf8) should contain only ("Test1", "Test2")
      } finally {
        stub.close()
      }
    }
  }
}
