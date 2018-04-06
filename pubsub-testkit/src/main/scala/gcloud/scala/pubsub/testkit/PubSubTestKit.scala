package gcloud.scala.pubsub.testkit

import java.util.UUID

import com.google.api.gax.core.NoCredentialsProvider
import com.google.pubsub.v1
import com.google.pubsub.v1.PubsubMessage
import gcloud.scala.pubsub._
import gcloud.scala.pubsub.syntax._
import gcloud.scala.pubsub.testkit.Lazy._
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.language.implicitConversions
import scala.util.Try

trait PubSubTestKit extends BeforeAndAfterAll {
  this: Suite with PubSubEmulator =>

  type PubSubTestSettings = (v1.ProjectName, v1.TopicName, v1.SubscriptionName)

  implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  def pubSubUrl: String = pubSubEmulatorUrl

  private val subscriptionAdminClientLazy
    : Lazy[com.google.cloud.pubsub.v1.SubscriptionAdminClient] = lazily {
    SubscriptionAdminClient(
      ((pubSubUrl: PubSubUrl): SubscriptionAdminClient.Settings)
        .copy(credentialsProvider = new NoCredentialsProvider())
    )
  }
  def subscriptionAdminClient = subscriptionAdminClientLazy()

  private val topicAdminClientLazy: Lazy[com.google.cloud.pubsub.v1.TopicAdminClient] = lazily {
    TopicAdminClient(
      ((pubSubUrl: PubSubUrl): TopicAdminClient.Settings)
        .copy(credentialsProvider = new NoCredentialsProvider())
    )
  }
  def topicAdminClient = topicAdminClientLazy()

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

    val publisher = PublisherStub(
      ((pubSubUrl: PubSubUrl): PublisherStub.Settings)
        .copy(credentialsProvider = new NoCredentialsProvider())
    )

    try {
      Await
        .result(publisher.publishAsync(topic = topic, messages = messages.map(conv)),
                publishTimeout)
        .getMessageIdsList
        .asScala
    } finally {
      publisher.close()
    }
  }

  def pullMessages(settings: PubSubTestSettings, amount: Int = Int.MaxValue): Seq[PubsubMessage] = {
    val (_, _, subscription) = settings

    val subscriber = SubscriberStub(
      ((pubSubUrl: PubSubUrl): SubscriberStub.Settings)
        .copy(credentialsProvider = new NoCredentialsProvider())
    )

    try {
      var lastUpdate = System.nanoTime()
      var cancel     = false

      val messages = collection.mutable.ArrayBuffer[PubsubMessage]()

      while (!cancel && messages.size < amount) {
        if (System.nanoTime() - lastUpdate > 1.second.toNanos) {
          cancel = true
        } else {
          messages ++= Await
            .result(subscriber.pullAsync(maxMessages = amount,
                                         returnImmediately = true,
                                         subscription = subscription),
                    10.seconds)
            .receivedMessages
            .map(_.getMessage)
          lastUpdate = System.nanoTime()
        }
      }

      Seq(messages: _*).take(amount)
    } finally {
      subscriber.close()
    }
  }
}
