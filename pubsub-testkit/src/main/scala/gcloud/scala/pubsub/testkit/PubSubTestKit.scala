package gcloud.scala.pubsub.testkit

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.google.cloud.pubsub.v1.{AckReplyConsumer, MessageReceiver}
import com.google.pubsub.v1
import com.google.pubsub.v1.PubsubMessage
import gcloud.scala.pubsub.FutureConversions._
import gcloud.scala.pubsub._
import org.scalatest.Suite

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.language.implicitConversions

trait PubSubTestKit extends LocalPubSub {
  this: Suite =>

  type PubSubTestSettings = (v1.ProjectName, v1.TopicName, v1.SubscriptionName)

  implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  def pubSubUrl: String = pubSubEmulatorUrl

  lazy val subscriptionAdminClient = SubscriptionAdminClient(pubSubUrl)
  lazy val topicAdminClient        = TopicAdminClient(pubSubUrl)

  val createTimeout: FiniteDuration  = 10.seconds
  val pullTimeout: FiniteDuration    = 10.seconds
  val publishTimeout: FiniteDuration = 10.seconds

  def newTestSetup(): PubSubTestSettings = {
    val project      = ProjectName(s"test-${UUID.randomUUID().toString}")
    val topic        = TopicName(project, "top")
    val subscription = SubscriptionName(project, "subs")

    Await.ready(topicAdminClient.createTopicAsync(Topic(topic)), createTimeout)
    Await.ready(subscriptionAdminClient.createSubscriptionAsync(Subscription(subscription, topic)),
                createTimeout)

    (project, topic, subscription)
  }

  def publishMessages[T](settings: PubSubTestSettings,
                         messages: T*)(implicit conv: T => PubsubMessage): Seq[String] = {
    val (_, topic, _) = settings

    val publisher = Publisher(topic, pubSubUrl)

    try {
      messages
        .map(conv)
        .map(publisher.publish)
        .map(_.asScala)
        .map(Await.result(_, publishTimeout))
    } finally {
      publisher.shutdown()
    }
  }

  def pullMessages(settings: PubSubTestSettings, amount: Int = Int.MaxValue): Seq[PubsubMessage] = {
    val (_, _, subscription) = settings

    val messages = collection.mutable.ArrayBuffer[PubsubMessage]()

    //noinspection ConvertExpressionToSAM (needed for scala 2.11)
    val subscriber =
      Subscriber(
        subscription,
        new MessageReceiver {
          override def receiveMessage(message: PubsubMessage, consumer: AckReplyConsumer): Unit = {
            messages += message
            consumer.ack()
          }
        },
        pubSubUrl
      )

    subscriber.startAsync().awaitRunning(10, TimeUnit.SECONDS)

    var lastMessages = 0
    var lastUpdate   = System.nanoTime()
    var cancel       = false

    while (!cancel && messages.size < amount) {
      if (lastMessages != messages.size) {
        lastMessages = messages.size
        lastUpdate = System.nanoTime()
      } else if (System.nanoTime() - lastUpdate > 1.second.toNanos) {
        cancel = true
      }
    }

    subscriber.stopAsync().awaitTerminated(10, TimeUnit.SECONDS)

    Seq(messages: _*).take(amount)
  }
}
