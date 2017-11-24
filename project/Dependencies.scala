import sbt._

object Dependencies {
  lazy val ScalaTest          = "org.scalatest"          %% "scalatest"                      % "3.0.3"
  lazy val ScalapbRuntime     = "com.trueaccord.scalapb" %% "scalapb-runtime"                % com.trueaccord.scalapb.compiler.Version.scalapbVersion
  lazy val ScalapbRuntimeGrpc = "com.trueaccord.scalapb" %% "scalapb-runtime-grpc"           % com.trueaccord.scalapb.compiler.Version.scalapbVersion
  lazy val GrpcNetty          = "io.grpc"                % "grpc-netty"                      % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion
  lazy val GrpcAuth           = "io.grpc"                % "grpc-auth"                       % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion
  lazy val GoogleOauth2       = "com.google.auth"        % "google-auth-library-oauth2-http" % "0.8.0"
  lazy val Slf4jApi           = "org.slf4j"              % "slf4j-api"                       % "1.7.25"
  lazy val LogbackClassic     = "ch.qos.logback"         % "logback-classic"                 % "1.2.1"

  lazy val proto = Seq(
    ScalapbRuntime % "protobuf",
    ScalapbRuntimeGrpc,
    GrpcNetty
  )

  lazy val pubSub = Seq(
    GoogleOauth2,
    GrpcAuth,
    ScalaTest % Test
  )

  lazy val pubSubTestKit = Seq(
    ScalaTest,
    Slf4jApi,
    LogbackClassic % Test
  )
}
