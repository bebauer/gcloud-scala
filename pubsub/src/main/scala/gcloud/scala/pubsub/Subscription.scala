package gcloud.scala.pubsub

import com.google.pubsub.v1

object Subscription {
  def apply(subscriptionName: v1.ProjectSubscriptionName): v1.Subscription.Builder =
    v1.Subscription
      .newBuilder()
      .setName(subscriptionName.toString)

  def apply(subscriptionName: v1.ProjectSubscriptionName,
            topicName: v1.ProjectTopicName): v1.Subscription.Builder =
    Subscription(subscriptionName)
      .setTopic(topicName.fullName)
}
