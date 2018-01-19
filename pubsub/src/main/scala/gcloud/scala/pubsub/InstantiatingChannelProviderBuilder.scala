package gcloud.scala.pubsub

import com.google.api.gax.core.ExecutorProvider
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider
import com.google.api.gax.rpc.{HeaderProvider, TransportChannelProvider}
import org.threeten.bp

import scala.concurrent.duration.Duration

private[pubsub] object InstantiatingChannelProviderBuilder {
  def apply(
      builder: InstantiatingGrpcChannelProvider.Builder
  ): InstantiatingChannelProviderBuilder = new InstantiatingChannelProviderBuilder(builder)
}

private[pubsub] class InstantiatingChannelProviderBuilder(
    builder: InstantiatingGrpcChannelProvider.Builder
) extends ChannelProviderBuilder {
  override def keepAliveTimeout(keepAliveTimeout: Duration): ChannelProviderBuilder = {
    builder.setKeepAliveTimeout(bp.Duration.ofNanos(keepAliveTimeout.toNanos))
    this
  }

  override def keepAliveTime(keepAliveTime: Duration): ChannelProviderBuilder = {
    builder.setKeepAliveTime(bp.Duration.ofNanos(keepAliveTime.toNanos))
    this
  }

  override def keepAliveWithoutCalls(keepAliveWithoutCalls: Boolean): ChannelProviderBuilder = {
    builder.setKeepAliveWithoutCalls(keepAliveWithoutCalls)
    this
  }

  override def endpoint(endpoint: String): ChannelProviderBuilder = {
    builder.setEndpoint(endpoint)
    this
  }

  override def executorProvider(executorProvider: ExecutorProvider): ChannelProviderBuilder = {
    builder.setExecutorProvider(executorProvider)
    this
  }

  override def headerProvider(headerProvider: HeaderProvider): ChannelProviderBuilder = {
    builder.setHeaderProvider(headerProvider)
    this
  }

  override def maxInboundMessageSize(maxInboundMessageSize: Int): ChannelProviderBuilder = {
    builder.setMaxInboundMessageSize(maxInboundMessageSize)
    this
  }

  override def build(): TransportChannelProvider = builder.build()
}
