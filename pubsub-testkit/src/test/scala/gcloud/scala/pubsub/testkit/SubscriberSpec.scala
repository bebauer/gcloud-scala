package gcloud.scala.pubsub.testkit

import java.util.concurrent.TimeUnit

import com.google.api.gax.core.NoCredentialsProvider
import gcloud.scala.pubsub._
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class SubscriberSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with Eventually
    with PubSubTestKit
    with DockerPubSub {

  override implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  "Subscriber" should {
    "pull published messages" in {
      val settings             = newTestSetup()
      val (_, _, subscription) = settings

      val messages = ArrayBuffer[String]()

      implicit val stringDecoder: MessageDataDecoder[String] = _.toStringUtf8

      val subscriber = Subscriber(subscription, pubSubUrl, new NoCredentialsProvider()) {
        (message, consumer) =>
          println(message.dataAs[String])
          messages += message.dataAs[String]
          consumer.ack()
      }

      subscriber.startAsync().awaitRunning(5, TimeUnit.SECONDS)

      publishMessages(settings, "TEST1", "TEST2", "TEST3") should have size 3

      implicit val patienceConfig = PatienceConfig(10.seconds, 500.milliseconds)

      eventually {
        messages should contain only ("TEST1", "TEST2", "TEST3")
      }

      subscriber.stopAsync().awaitTerminated(5, TimeUnit.SECONDS)
    }
  }
}
