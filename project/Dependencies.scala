import sbt._

object Dependencies {
  lazy val ScalaTest      = "org.scalatest"    %% "scalatest"          % "3.0.3"
  lazy val GcloudPubSub   = "com.google.cloud" % "google-cloud-pubsub" % "0.33.0-beta"
  lazy val Slf4jApi       = "org.slf4j"        % "slf4j-api"           % "1.7.25"
  lazy val LogbackClassic = "ch.qos.logback"   % "logback-classic"     % "1.2.1"

  lazy val pubSub = Seq(
    GcloudPubSub,
    ScalaTest % Test
  )

  lazy val pubSubTestKit = Seq(
    ScalaTest,
    Slf4jApi,
    LogbackClassic % Test
  )
}
