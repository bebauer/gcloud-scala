package gcloud.scala.pubsub

import com.google.auth.Credentials
import com.google.auth.oauth2.GoogleCredentials
import gcloud.scala.pubsub.retry.RetryScheduler
import io.grpc.ManagedChannel

import scala.concurrent.ExecutionContextExecutor

object PubSubClientConfig {
  import scala.collection.JavaConverters._

  /** Type definition for a credentials provider */
  type CredentialsProvider = () => Credentials

  /**
    * Channel provider trait. Implement this trait to customize which Netty channels the client should use.
    * E.g. one channel for all instances.
    */
  trait ChannelProvider {

    /**
      * Gets a [[ClientChannel]]. Pass a [[ExecutionContextExecutor]] here to overwrite the default.
      * This depends on the channel provider implementation.
      *
      * @param maybeExecutor possible [[ExecutionContextExecutor]]
      * @return the client channel
      */
    def channel(maybeExecutor: Option[ExecutionContextExecutor] = None): ClientChannel

    /**
      * Close a client channel.
      *
      * @param channel the client channel
      */
    def closeChannel(channel: ClientChannel)
  }

  /**
    * Client channel class which holds the [[ManagedChannel]] and the [[ExecutionContextExecutor]] set on this channel.
    *
    * @param channel the channel
    * @param executor the [[ExecutionContextExecutor]]
    */
  case class ClientChannel(channel: ManagedChannel, executor: ExecutionContextExecutor)

  /** Default service scopes for the Pub/Sub */
  final val DefaultServiceScopes =
    List("https://www.googleapis.com/auth/cloud-platform", "https://www.googleapis.com/auth/pubsub")

  /** Default value for inbound message size */
  final val DefaultMaxInboundMessageSize = 4 * 1024 * 1024 // 4MB

  /** Default value for a credentials provider */
  final val DefaultCredentialsProvider = () =>
    GoogleCredentials.getApplicationDefault.createScoped(DefaultServiceScopes.asJava)

  /**
    * Create a [[PubSubClientConfig]] from an URL.
    *
    * @param url the Pub/Sub URL
    * @return the client config
    */
  def apply(url: PubSubUrl): PubSubClientConfig =
    apply(url, DefaultMaxInboundMessageSize)

  /**
    * Create a [[PubSubClientConfig]] from an URL and maximum message size.
    *
    * @param url the Pub/Sub URL
    * @param maxInboundMessageSize the maximum size in bytes for inbound messages
    * @return the client config
    */
  def apply(url: PubSubUrl, maxInboundMessageSize: Int): PubSubClientConfig =
    new PubSubClientConfig(DefaultChannelProvider(url, maxInboundMessageSize))
}

/**
  * Pub/Sub client configuration class.
  *
  * @param channelProvider the channel provider
  * @param credentialsProvider the credentials provider
  * @param retryScheduler the retry scheduler
  */
case class PubSubClientConfig(
    channelProvider: PubSubClientConfig.ChannelProvider = DefaultChannelProvider(),
    credentialsProvider: PubSubClientConfig.CredentialsProvider =
      PubSubClientConfig.DefaultCredentialsProvider,
    retryScheduler: RetryScheduler = RetryScheduler()
)
