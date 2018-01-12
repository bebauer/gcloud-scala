package gcloud.scala.pubsub

import com.google.auth.Credentials
import gcloud.scala.pubsub.retry.RetryScheduler
import io.grpc.Channel

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
  * [[PubSubClient]] companion object. Use the [[PubSubClient.apply]] methods to create instances of the default client.
  */
object PubSubClient {

  /**
    * Creates the client with default settings.
    *
    * @param executor the [[ExecutionContextExecutor]] on which the client operations are performed
    * @return the PubSubClient
    */
  def apply()(implicit executor: ExecutionContextExecutor): PubSubClient =
    apply(PubSubUrl.DefaultPubSubUrl)

  /**
    * Create the client with the specified URL.
    *
    * @param pubSubUrl the Pub/Sub URL
    * @param executor the [[ExecutionContextExecutor]] on which the client operations are performed
    * @return the PubSubClient
    */
  def apply(pubSubUrl: PubSubUrl)(implicit executor: ExecutionContextExecutor): PubSubClient =
    apply(PubSubClientConfig(pubSubUrl))

  /**
    * Create the client with the specified config.
    *
    * @param config the Pub/Sub client config
    * @param executor the [[ExecutionContextExecutor]] on which the client operations are performed
    * @return the PubSubClient
    */
  def apply(config: PubSubClientConfig)(implicit executor: ExecutionContextExecutor): PubSubClient =
    new DefaultClient(config, Some(executor))

  private class DefaultClient(config: PubSubClientConfig,
                              executor: Option[ExecutionContextExecutor])
      extends PubSubClient {

    private val clientChannel = config.channelProvider.channel(executor)

    private val credentials = config.credentialsProvider()

    override def getChannel: Channel = clientChannel.channel

    override def getCredentials: Credentials = credentials

    override implicit val executionContext: ExecutionContext = clientChannel.executor

    override implicit val retryScheduler: RetryScheduler = config.retryScheduler

    override def close(): Unit =
      config.channelProvider.closeChannel(clientChannel)
  }
}

/**
  * The trait for a Pub/Sub client. For getting a default implementation see the [[PubSubClient]] companion object.
  */
trait PubSubClient extends PubSubPublisher with PubSubSubscriber
