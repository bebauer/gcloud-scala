package gcloud.scala.pubsub.testkit

import java.util.UUID

import com.google.protobuf.ByteString
import com.google.pubsub.v1._
import gcloud.scala.pubsub.{ProjectName, PubSubClient, SubscriptionName, TopicName}
import org.scalatest.Suite

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.language.implicitConversions

trait PubSubTestKit extends LocalPubSub {
  this: Suite =>

  type PubSubTestSettings = (ProjectName, TopicName, SubscriptionName)

  implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  def pubSubUrl: String = pubSubEmulatorUrl

  lazy val client: PubSubClient = PubSubClient(pubSubUrl)

  val createTimeout: FiniteDuration  = 10.seconds
  val pullTimeout: FiniteDuration    = 10.seconds
  val publishTimeout: FiniteDuration = 10.seconds

  def newTestSetup(): PubSubTestSettings = {
    val project      = ProjectName(s"test-${UUID.randomUUID().toString}")
    val topic        = TopicName(project, "top")
    val subscription = SubscriptionName(project, "subs")

    Await.ready(client.createTopic(Topic(topic.fullName)), createTimeout)
    Await.ready(client.createSubscription(Subscription(subscription.fullName, topic.fullName)),
                createTimeout)

    (project, topic, subscription)
  }

  def publishMessages[T](settings: PubSubTestSettings,
                         messages: T*)(implicit conv: T => PubsubMessage): Unit = {
    val (_, topic, _) = settings

    Await.ready(client.publish(topic, messages.map(conv).to[immutable.Seq]), publishTimeout)
  }

  def pullMessages(settings: PubSubTestSettings,
                   amount: Int,
                   strict: Boolean = false): Seq[ReceivedMessage] = {
    val (_, _, subscription) = settings

    val results =
      if (amount > 0)
        Await.result(
          client.pull(subscription, returnImmediately = true, maxMessages = Some(amount)),
          pullTimeout
        )
      else Seq()

    if (strict)
      if (results.isEmpty)
        results
      else
        results ++ pullMessages(settings, amount - results.size, strict)
    else
      results
  }

  implicit val stringToPubSubMessageConverter: String => PubsubMessage = (value: String) =>
    PubsubMessage(ByteString.copyFromUtf8(value))
}
