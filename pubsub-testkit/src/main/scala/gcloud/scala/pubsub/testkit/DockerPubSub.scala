package gcloud.scala.pubsub.testkit

import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import org.scalatest.Suite
import org.testcontainers.containers.wait.Wait

trait DockerPubSub extends PubSubEmulator with ForAllTestContainer {
  this: Suite =>

  override val container = GenericContainer(
    "google/cloud-sdk:latest",
    exposedPorts = Seq(8085),
    command = Seq("gcloud", "beta", "emulators", "pubsub", "start", "--host-port=0.0.0.0:8085"),
    waitStrategy = Wait.forListeningPort()
  )

  override def pubSubEmulatorUrl: String =
    s"http://${container.containerIpAddress}:${container.mappedPort(8085)}"
}
