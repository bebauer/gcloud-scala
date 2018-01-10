package gcloud.scala.pubsub

import com.google.auth.Credentials
import com.google.auth.oauth2.GoogleCredentials
import gcloud.scala.pubsub.retry.RetryScheduler
import io.grpc.ManagedChannel

import scala.concurrent.ExecutionContextExecutor

object PubSubClientConfig {
  import scala.collection.JavaConverters._

  type CredentialsProvider = () => Credentials

  trait ChannelProvider {
    def channel(maybeExecutor: Option[ExecutionContextExecutor] = None): ClientChannel

    def closeChannel(channel: ClientChannel)
  }

  case class ClientChannel(channel: ManagedChannel, executor: ExecutionContextExecutor)

  final val DefaultServiceScopes =
    List("https://www.googleapis.com/auth/cloud-platform", "https://www.googleapis.com/auth/pubsub")

  final val DefaultMaxInboundMessageSize = 4 * 1024 * 1024 // 4MB

  final val DefaultCredentialsProvider = () =>
    GoogleCredentials.getApplicationDefault.createScoped(DefaultServiceScopes.asJava)

  def apply(url: PubSubUrl): PubSubClientConfig =
    apply(url, DefaultMaxInboundMessageSize)

  def apply(url: PubSubUrl, maxInboundMessageSize: Int): PubSubClientConfig =
    new PubSubClientConfig(DefaultChannelProvider(url, maxInboundMessageSize))
}

case class PubSubClientConfig(
    channelProvider: PubSubClientConfig.ChannelProvider = DefaultChannelProvider(),
    credentialsProvider: PubSubClientConfig.CredentialsProvider =
      PubSubClientConfig.DefaultCredentialsProvider,
    retryScheduler: RetryScheduler = RetryScheduler()
)
