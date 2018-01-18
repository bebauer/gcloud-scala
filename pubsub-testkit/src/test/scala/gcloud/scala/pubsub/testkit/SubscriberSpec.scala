package gcloud.scala.pubsub.testkit

import java.util.concurrent.TimeUnit

import com.google.cloud.pubsub.v1.{AckReplyConsumer, MessageReceiver}
import com.google.pubsub.v1.PubsubMessage
import gcloud.scala.pubsub._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContextExecutor

class SubscriberSpec extends WordSpec with Matchers with ScalaFutures with PubSubTestKit {

  override implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  "Subscriber" should {
    "pull published messages" in {
      val settings             = newTestSetup()
      val (_, _, subscription) = settings

      val messages = ArrayBuffer[String]()

      //noinspection ConvertExpressionToSAM (needed for scala 2.11)
      val subscriber = Subscriber(
        subscription,
        new MessageReceiver {
          override def receiveMessage(message: PubsubMessage, consumer: AckReplyConsumer): Unit = {
            messages += message.getData.toStringUtf8
            consumer.ack()
          }
        },
        pubSubUrl
      )

      subscriber.startAsync().awaitRunning(5, TimeUnit.SECONDS)

      publishMessages(settings, "TEST1", "TEST2", "TEST3") should have size 3

      eventually {
        messages shouldEqual Seq("TEST1", "TEST2", "TEST3")
      }

      subscriber.stopAsync().awaitTerminated(5, TimeUnit.SECONDS)
    }
  }
}
