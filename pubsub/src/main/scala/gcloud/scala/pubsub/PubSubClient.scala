package gcloud.scala.pubsub

import com.google.auth.Credentials
import io.grpc.Channel

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object PubSubClient {
  def apply()(implicit executor: ExecutionContextExecutor): PubSubClient =
    apply(PubSubUrl.DefaultPubSubUrl)

  def apply(pubSubUrl: PubSubUrl)(implicit executor: ExecutionContextExecutor): PubSubClient =
    apply(PubSubClientConfig(pubSubUrl))

  def apply(config: PubSubClientConfig)(implicit executor: ExecutionContextExecutor): PubSubClient =
    new DefaultClient(config, Some(executor))

  private class DefaultClient(config: PubSubClientConfig,
                              executor: Option[ExecutionContextExecutor])
      extends PubSubClient {
    import config._

    private val clientChannel = channelProvider.channel(executor)

    private val credentials = credentialsProvider()

    override def getChannel: Channel = clientChannel.channel

    override def getCredentials: Credentials = credentials

    override implicit val executionContext: ExecutionContext = clientChannel.executor

    override def close(): Unit =
      clientChannel.channel.shutdown()

  }
}

trait PubSubClient extends PubSubPublisher with PubSubSubscriber
