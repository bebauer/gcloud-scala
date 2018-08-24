package gcloud.scala.pubsub

import java.util
import java.util.concurrent.{ScheduledExecutorService, TimeUnit}

import com.google.api.gax.core.ExecutorProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.{
  FixedTransportChannelProvider,
  HeaderProvider,
  TransportChannel,
  TransportChannelProvider
}
import io.grpc.ManagedChannelBuilder

import scala.concurrent.duration.Duration

private[pubsub] object FixedAutoClosableChannelProviderBuilder {
  def apply(host: String, port: Int): FixedAutoClosableChannelProviderBuilder =
    new FixedAutoClosableChannelProviderBuilder(host, port)
}

private[pubsub] class FixedAutoClosableChannelProviderBuilder(host: String, port: Int)
    extends ChannelProviderBuilder {
  private val channelBuilder = ManagedChannelBuilder
    .forAddress(host, port)
    .usePlaintext()

  override def keepAliveTimeout(keepAliveTimeout: Duration): ChannelProviderBuilder = {
    channelBuilder.keepAliveTimeout(keepAliveTimeout.toNanos, TimeUnit.NANOSECONDS)
    this
  }

  override def keepAliveTime(keepAliveTime: Duration): ChannelProviderBuilder = {
    channelBuilder.keepAliveTime(keepAliveTime.toNanos, TimeUnit.NANOSECONDS)
    this
  }

  override def keepAliveWithoutCalls(
      keepAliveWithoutCalls: Boolean
  ): ChannelProviderBuilder = {
    channelBuilder.keepAliveWithoutCalls(keepAliveWithoutCalls)
    this
  }

  override def endpoint(endpoint: String): ChannelProviderBuilder = this

  override def build(): TransportChannelProvider =
    new TransportChannelProvider {
      private val provider =
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channelBuilder.build()))

      override def needsHeaders(): Boolean = provider.needsHeaders()

      override def getTransportChannel: TransportChannel = provider.getTransportChannel

      override def getTransportName: String = provider.getTransportName

      override def withHeaders(headers: util.Map[String, String]): TransportChannelProvider =
        provider.withHeaders(headers)

      override def withEndpoint(endpoint: String): TransportChannelProvider =
        provider.withEndpoint(endpoint)

      override def withExecutor(executor: ScheduledExecutorService): TransportChannelProvider =
        provider.withExecutor(executor)

      override def shouldAutoClose(): Boolean = true

      override def needsExecutor(): Boolean = provider.needsExecutor()

      override def needsEndpoint(): Boolean = provider.needsEndpoint()

      override def acceptsPoolSize(): Boolean = provider.acceptsPoolSize()

      override def withPoolSize(size: Int): TransportChannelProvider = provider.withPoolSize(size)
    }

  override def executorProvider(
      executorProvider: ExecutorProvider
  ): ChannelProviderBuilder = {
    channelBuilder.equals(executorProvider.getExecutor)
    this
  }

  override def headerProvider(headerProvider: HeaderProvider): ChannelProviderBuilder = this

  override def maxInboundMessageSize(maxInboundMessageSize: Int): ChannelProviderBuilder = {
    channelBuilder.maxInboundMessageSize(maxInboundMessageSize)
    this
  }
}
