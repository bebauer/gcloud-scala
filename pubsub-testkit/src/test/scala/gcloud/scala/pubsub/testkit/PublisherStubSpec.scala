package gcloud.scala.pubsub.testkit

import com.google.api.gax.core.NoCredentialsProvider
import gcloud.scala.pubsub._
import gcloud.scala.pubsub.syntax._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}

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

      val stub = PublisherStub(pubSubUrl, NoCredentialsProvider.create())

      try {
        Await.ready(
          stub.publishAsync(topic = topic, messages = Seq("Test1")),
          20.seconds
        )
        Await.ready(stub.publishAsync(topic = topic, messages = Seq(PubsubMessage("Test2"))),
                    20.seconds)

        pullMessages(settings, 2).map(_.getData.toStringUtf8) should contain only ("Test1", "Test2")
      } finally {
        stub.close()
      }
    }
  }
}
