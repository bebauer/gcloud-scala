package gcloud.scala.pubsub

import com.google.protobuf.ByteString
import com.google.pubsub.v1

import scala.collection.JavaConverters._

object PubsubMessage {
  type MessageDataEncoder[T] = T => ByteString

  def apply[T](data: T, attributes: Map[String, String] = Map())(
      implicit encoder: MessageDataEncoder[T]
  ): v1.PubsubMessage.Builder =
    v1.PubsubMessage.newBuilder().setData(encoder(data)).putAllAttributes(attributes.asJava)
}
