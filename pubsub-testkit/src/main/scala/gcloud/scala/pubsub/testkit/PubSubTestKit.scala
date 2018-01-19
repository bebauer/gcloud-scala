package gcloud.scala.pubsub.testkit

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.google.pubsub.v1
import com.google.pubsub.v1.PubsubMessage
import gcloud.scala.pubsub.FutureConversions._
import gcloud.scala.pubsub._
import gcloud.scala.pubsub.testkit.Lazy._
import org.scalatest.Suite

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.language.implicitConversions
import scala.util.Try

trait PubSubTestKit extends LocalPubSub {
  this: Suite =>

  type PubSubTestSettings = (v1.ProjectName, v1.TopicName, v1.SubscriptionName)

  implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  def pubSubUrl: String = pubSubEmulatorUrl

  private val subscriptionAdminClientLazy
    : Lazy[com.google.cloud.pubsub.v1.SubscriptionAdminClient] = lazily {
    SubscriptionAdminClient(pubSubUrl)
  }
  lazy val subscriptionAdminClient = subscriptionAdminClientLazy()

  private val topicAdminClientLazy: Lazy[com.google.cloud.pubsub.v1.TopicAdminClient] = lazily {
    TopicAdminClient(pubSubUrl)
  }
  lazy val topicAdminClient = topicAdminClientLazy()

  val createTimeout: FiniteDuration  = 10.seconds
  val publishTimeout: FiniteDuration = 10.seconds

  override protected def afterAll(): Unit = {
    Try {
      topicAdminClientLazy.foreach(_.close())
      subscriptionAdminClientLazy.foreach(_.close())
    }

    super.afterAll()
  }

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

    val subscriber =
      Subscriber(subscription, pubSubUrl) { (message, consumer) =>
        messages += message
        consumer.ack()
      }

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
