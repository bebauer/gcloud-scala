package gcloud.scala.pubsub

import com.google.pubsub.v1

object Subscription {
  def apply(subscriptionName: v1.SubscriptionName): v1.Subscription.Builder =
    v1.Subscription
      .newBuilder()
      .setNameWithSubscriptionName(subscriptionName)

  def apply(subscriptionName: v1.SubscriptionName,
            topicName: v1.TopicName): v1.Subscription.Builder =
    Subscription(subscriptionName)
      .setTopic(topicName.fullName)
}
