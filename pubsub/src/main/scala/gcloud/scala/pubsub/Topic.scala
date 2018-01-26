package gcloud.scala.pubsub

import com.google.pubsub.v1

object Topic {
  def apply(topicName: v1.TopicName): v1.Topic =
    v1.Topic.newBuilder().setName(topicName.toString).build()
}
