import sbt._

object Dependencies {
  lazy val ScalaTest           = "org.scalatest"    %% "scalatest"            % "3.0.3"
  lazy val GcloudPubSub        = "com.google.cloud" % "google-cloud-pubsub"   % "0.34.0-beta"
  lazy val Slf4jApi            = "org.slf4j"        % "slf4j-api"             % "1.7.25"
  lazy val LogbackClassic      = "ch.qos.logback"   % "logback-classic"       % "1.2.1"
  lazy val TestcontainersScala = "com.dimafeng"     %% "testcontainers-scala" % "0.16.0"
  lazy val ScalaMeta           = "org.scalameta"    %% "scalameta"            % "3.3.0"
  lazy val Treehugger          = "com.eed3si9n"     %% "treehugger"           % "0.4.3"

  lazy val pubSub = Seq(
    GcloudPubSub,
    ScalaTest % Test
  )

  lazy val pubSubTestKit = Seq(
    ScalaTest,
    Slf4jApi,
    TestcontainersScala,
    LogbackClassic % Test
  )

  lazy val codeGen = Seq(
    GcloudPubSub,
    Treehugger
  )
}
