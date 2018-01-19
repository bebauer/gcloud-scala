package gcloud.scala.pubsub

import java.net.URL
import java.util.concurrent.TimeUnit

import com.google.api.gax.core.ExecutorProvider
import com.google.api.gax.grpc.{GrpcTransportChannel, InstantiatingGrpcChannelProvider}
import com.google.api.gax.rpc.{
  FixedTransportChannelProvider,
  HeaderProvider,
  TransportChannelProvider
}
import io.grpc.ManagedChannelBuilder

import scala.concurrent.duration.Duration
import scala.language.implicitConversions

object PubSubUrl {
  final val DefaultPubSubUrl: PubSubUrl = "https://pubsub.googleapis.com:443"

  implicit def fromString(url: String): PubSubUrl = new URL(url)

  implicit def fromUrl(url: URL): PubSubUrl = {
    val tlsEnabled = url.getProtocol match {
      case "http"  => false
      case "https" => true
    }

    PubSubUrl(url.getHost, url.getPort, tlsEnabled)
  }
}

case class PubSubUrl(host: String, port: Int, tlsEnabled: Boolean) {
  private[pubsub] def channelProviderBuilder(): ChannelProviderBuilder =
    if (tlsEnabled) {
      InstantiatingGrpcChannelProvider.newBuilder().setEndpoint(s"$host:$port")
    } else {
      FixedAutoClosableChannelProviderBuilder(host, port)
    }
}
