lazy val `gcloud-scala` = (project in file("."))
  .settings(
    inThisBuild(commonSettings)
  )
  .aggregate(`gcloud-scala-proto`, `gcloud-scala-pubsub`, `gcloud-scala-pubsub-testkit`)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val `gcloud-scala-pubsub` =
  (project in file("pubsub"))
    .settings(libraryDependencies ++= Dependencies.pubSub)
    .dependsOn(`gcloud-scala-proto`)

lazy val `gcloud-scala-pubsub-testkit` =
  (project in file("pubsub-testkit"))
    .settings(libraryDependencies ++= Dependencies.pubSubTestKit)
    .dependsOn(`gcloud-scala-pubsub`)

lazy val `gcloud-scala-proto` = (project in file("proto")).settings(
  fetchProtos := {
    if (java.nio.file.Files.notExists(new File("proto/target/protobuf").toPath)) {
      println("Path does not exist, downloading...")
      IO.unzipURL(
        from = new URL("https://github.com/googleapis/googleapis/archive/master.zip"),
        toDirectory = new File("proto/target/protobuf"),
        filter = "googleapis-master/google/api/*" | "googleapis-master/google/pubsub/v1/*" | "googleapis-master/google/rpc/*" |
        "googleapis-master/google/type/*" | "googleapis-master/google/logging/*" | "googleapis-master/google/longrunning/*"
      )
    } else {
      println("Path exists, no need to download.")
    }
  },
  compile in Compile := (compile in Compile).dependsOn(fetchProtos).value,
  libraryDependencies ++= Dependencies.proto,
  PB.targets in Compile := Seq(
    scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
  ),
  PB.protoSources in Compile += target.value / "protobuf" / "googleapis-master"
)

lazy val fetchProtos = taskKey[Unit]("Download Google protos and extract to target.")

lazy val commonSettings = Seq(
  organization := "de.codecentric",
  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.11.11", "2.12.4"),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-target:jvm-1.8",
    "-encoding",
    "UTF-8"
  ),
  javacOptions ++= Seq(
    "-source",
    "1.8",
    "-target",
    "1.8"
  ),
  fork in Test := true,
  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
)
