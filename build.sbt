lazy val `gcloud-scala` = (project in file("."))
  .settings(
    inThisBuild(commonSettings)
  )
  .aggregate(`gcloud-scala-pubsub`, `gcloud-scala-pubsub-testkit`)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val `gcloud-scala-pubsub` =
  (project in file("pubsub"))
    .settings(libraryDependencies ++= Dependencies.pubSub)

lazy val `gcloud-scala-pubsub-testkit` =
  (project in file("pubsub-testkit"))
    .settings(libraryDependencies ++= Dependencies.pubSubTestKit)
    .dependsOn(`gcloud-scala-pubsub`)

lazy val commonSettings = Seq(
  organization := "gcloud-scala",
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
