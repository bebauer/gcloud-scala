package gcloud.scala.pubsub.testkit

import java.util.UUID

import com.google.api.gax.core.NoCredentialsProvider
import com.google.pubsub.v1
import com.google.pubsub.v1.{PubsubMessage, ReceivedMessage}
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

  type PubSubTestSettings = (v1.ProjectName, v1.ProjectTopicName, v1.ProjectSubscriptionName)

  implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  def pubSubUrl: String = pubSubEmulatorUrl

  private val subscriptionAdminClientLazy
    : Lazy[com.google.cloud.pubsub.v1.SubscriptionAdminClient] = lazily {
    SubscriptionAdminClient(
      SubscriptionAdminClient
        .Settings(pubSubUrl)
        .copy(credentialsProvider = new NoCredentialsProvider())
    )
  }
  def subscriptionAdminClient = subscriptionAdminClientLazy()

  private val topicAdminClientLazy: Lazy[com.google.cloud.pubsub.v1.TopicAdminClient] = lazily {
    TopicAdminClient(
      TopicAdminClient
        .Settings(pubSubUrl)
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
    val topic        = ProjectTopicName(project, "top")
    val subscription = ProjectSubscriptionName(project, "subs")

    Await.ready(topicAdminClient.createTopicAsync(Topic(topic)), createTimeout)
    Await.ready(subscriptionAdminClient.createSubscriptionAsync(Subscription(subscription, topic)),
                createTimeout)

    (project, topic, subscription)
  }

  def publishMessages[T](settings: PubSubTestSettings,
                         messages: T*)(implicit conv: T => PubsubMessage): Seq[String] = {
    val (_, topic, _) = settings

    val publisher = PublisherStub(
      PublisherStub
        .Settings(pubSubUrl)
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

  def pullMessages(settings: PubSubTestSettings, amount: Int = Int.MaxValue): Seq[PubsubMessage] =
    pullReceivedMessages(settings, amount).map(_.getMessage)

  def pullReceivedMessages(settings: PubSubTestSettings,
                           amount: Int = Int.MaxValue): Seq[ReceivedMessage] = {
    val (_, _, subscription) = settings

    val subscriber = SubscriberStub(
      SubscriberStub
        .Settings(pubSubUrl)
        .copy(credentialsProvider = new NoCredentialsProvider())
    )

    try {
      var lastUpdate = System.nanoTime()
      var cancel     = false

      val messages = collection.mutable.ArrayBuffer[ReceivedMessage]()

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
          lastUpdate = System.nanoTime()
        }
      }

      Seq(messages: _*).take(amount)
    } finally {
      subscriber.close()
    }
  }
}
