package gcloud.scala.pubsub

import com.google.auth.Credentials
import com.google.auth.oauth2.GoogleCredentials
import io.grpc.ManagedChannel
import io.grpc.netty.{NegotiationType, NettyChannelBuilder}

import scala.concurrent.ExecutionContextExecutor

object PubSubClientConfig {
  import scala.collection.JavaConverters._

  type CredentialsProvider = () => Credentials

  trait ChannelProvider {
    def channel(maybeExecutor: Option[ExecutionContextExecutor] = None): ClientChannel
  }

  case class ClientChannel(channel: ManagedChannel, executor: ExecutionContextExecutor)

  case class DefaultChannelProvider(url: PubSubUrl = PubSubUrl.DefaultPubSubUrl,
                                    maxInboundMessageSize: Int = DefaultMaxInboundMessageSize)
      extends ChannelProvider {
    override def channel(maybeExecutor: Option[ExecutionContextExecutor]): ClientChannel =
      maybeExecutor match {
        case Some(executor) =>
          ClientChannel(
            channel = NettyChannelBuilder
              .forAddress(url.host, url.port)
              .maxInboundMessageSize(maxInboundMessageSize)
              .flowControlWindow(5000000)
              .negotiationType(
                if (url.tlsEnabled) NegotiationType.TLS
                else NegotiationType.PLAINTEXT
              )
              .executor(executor)
              .build(),
            executor = executor
          )
        case None =>
          throw new IllegalArgumentException(
            "An executor has to be defined for the default channel provider."
          )
      }
  }

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
    channelProvider: PubSubClientConfig.ChannelProvider =
      PubSubClientConfig.DefaultChannelProvider(),
    credentialsProvider: PubSubClientConfig.CredentialsProvider =
      PubSubClientConfig.DefaultCredentialsProvider
)
