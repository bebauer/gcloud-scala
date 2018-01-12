package gcloud.scala.pubsub

import gcloud.scala.pubsub.PubSubClientConfig.{
  ChannelProvider,
  ClientChannel,
  DefaultMaxInboundMessageSize
}
import io.grpc.netty.{NegotiationType, NettyChannelBuilder}

import scala.concurrent.ExecutionContextExecutor

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

  override def closeChannel(channel: ClientChannel): Unit = channel.channel.shutdown()
}
