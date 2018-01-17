package gcloud.scala.pubsub

import java.net.URL

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
  val url = s"$host:$port"
}
