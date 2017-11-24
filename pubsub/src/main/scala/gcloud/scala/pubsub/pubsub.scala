package gcloud.scala

import scala.language.implicitConversions

package object pubsub {

  type Seq[+A] = scala.collection.immutable.Seq[A]

  val Seq = scala.collection.immutable.Seq

  object ProjectName {
    private val ProjectNamePattern = "(projects/)?(.+)".r

    implicit def fromString(name: String): ProjectName = ProjectName(name)

    def apply(name: String): ProjectName =
      new ProjectName(name match {
        case ProjectNamePattern(n)    => n
        case ProjectNamePattern(_, n) => n
        case _ =>
          throw new IllegalArgumentException(
            s"Project name '$name' does not match pattern '$ProjectNamePattern'."
          )
      })
  }

  class ProjectName(val name: String) {
    val fullName: String = s"projects/$name"
  }

  object TopicName {
    private val TopicNamePattern = "projects/(.+)/topics/(.+)".r

    implicit def fromString(fullName: String): TopicName = TopicName(fullName)

    def apply(fullName: String): TopicName = fullName match {
      case TopicNamePattern(project, topic) => TopicName(project, topic)
      case _ =>
        throw new IllegalArgumentException(
          s"Full topic name '$fullName' does not match pattern '$TopicNamePattern'."
        )
    }

    def apply(projectName: ProjectName, name: String): TopicName = new TopicName(projectName, name)
  }

  class TopicName(val projectName: ProjectName, val name: String) {
    val fullName: String = s"${projectName.fullName}/topics/$name"
  }

  object SubscriptionName {
    private val SubscriptionNamePattern = "projects/(.+)/subscriptions/(.+)".r

    implicit def fromString(fullName: String): SubscriptionName = SubscriptionName(fullName)

    def apply(fullName: String): SubscriptionName = fullName match {
      case SubscriptionNamePattern(project, subscription) =>
        SubscriptionName(project, subscription)
      case _ =>
        throw new IllegalArgumentException(
          s"Full subscription name '$fullName' does not match pattern '$SubscriptionNamePattern'."
        )
    }

    def apply(projectName: ProjectName, name: String): SubscriptionName =
      new SubscriptionName(projectName, name)
  }

  class SubscriptionName(val projectName: ProjectName, val name: String) {
    val fullName: String = s"${projectName.fullName}/subscriptions/$name"
  }
}
