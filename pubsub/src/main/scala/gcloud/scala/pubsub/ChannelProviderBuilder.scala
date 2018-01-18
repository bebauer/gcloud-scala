package gcloud.scala.pubsub

import com.google.api.gax.core.ExecutorProvider
import com.google.api.gax.rpc.{HeaderProvider, TransportChannelProvider}

import scala.concurrent.duration.Duration

trait ChannelProviderBuilder {
  def executorProvider(executorProvider: ExecutorProvider): ChannelProviderBuilder

  def headerProvider(headerProvider: HeaderProvider): ChannelProviderBuilder

  def endpoint(endpoint: String): ChannelProviderBuilder

  def maxInboundMessageSize(maxInboundMessageSize: Int): ChannelProviderBuilder

  def keepAliveTime(keepAliveTime: Duration): ChannelProviderBuilder

  def keepAliveTimeout(keepAliveTimeout: Duration): ChannelProviderBuilder

  def keepAliveWithoutCalls(keepAliveWithoutCalls: Boolean): ChannelProviderBuilder

  def build(): TransportChannelProvider
}
