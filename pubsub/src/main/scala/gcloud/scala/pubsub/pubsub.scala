package gcloud.scala

import scala.language.implicitConversions

package object pubsub {

  type Seq[+A] = scala.collection.immutable.Seq[A]

  //noinspection TypeAnnotation
  val Seq = scala.collection.immutable.Seq

  object ProjectName {
    private val ProjectNamePattern = "(projects/)?(.+)".r

    /**
      * Implicitly converts a string to a [[ProjectName]] by calling [[ProjectName.apply()]].
      *
      * @param name the name
      * @return the [[ProjectName]]
      */
    implicit def fromString(name: String): ProjectName = ProjectName(name)

    /**
      * Creates a [[ProjectName]] by parsing a name string.
      * The name can be specified with or without the 'projects/' prefix.
      *
      * @param name the project name
      * @return the [[ProjectName]]
      * @throws IllegalArgumentException if the name cannot be parsed
      */
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

  /**
    * Class for a Google Cloud project name.
    *
    * @param name the name of the project
    */
  class ProjectName(val name: String) {
    val fullName: String = s"projects/$name"
  }

  object TopicName {
    private val TopicNamePattern = "projects/(.+)/topics/(.+)".r

    /**
      * Implicitly converts a string to a [[TopicName]] by calling [[TopicName.apply()]].
      *
      * @param fullName the full name
      * @return the [[TopicName]]
      */
    implicit def fromString(fullName: String): TopicName = TopicName(fullName)

    /**
      * Creates a [[TopicName]] by parsing the full name (projects/{project}/topics/{topic}) string.
      *
      * @param fullName the full name string
      * @return the [[TopicName]]
      * @throws IllegalArgumentException if the full name cannot be parsed
      */
    def apply(fullName: String): TopicName = fullName match {
      case TopicNamePattern(project, topic) => TopicName(project, topic)
      case _ =>
        throw new IllegalArgumentException(
          s"Full topic name '$fullName' does not match pattern '$TopicNamePattern'."
        )
    }

    /**
      * Creates a [[TopicName]] from a [[ProjectName]] and a topic name.
      *
      * @param projectName the [[ProjectName]]
      * @param name the topic name
      * @return the [[TopicName]]
      */
    def apply(projectName: ProjectName, name: String): TopicName = new TopicName(projectName, name)
  }

  /**
    * Class for a Google Cloud Pub/Sub topic name.
    *
    * @param projectName the name of the project
    * @param name the name of the topic
    */
  class TopicName(val projectName: ProjectName, val name: String) {
    val fullName: String = s"${projectName.fullName}/topics/$name"
  }

  object SubscriptionName {
    private val SubscriptionNamePattern = "projects/(.+)/subscriptions/(.+)".r

    /**
      * Implicitly converts a string to a [[SubscriptionName]] by calling [[SubscriptionName.apply()]].
      *
      * @param fullName the full name
      * @return the [[SubscriptionName]]
      */
    implicit def fromString(fullName: String): SubscriptionName = SubscriptionName(fullName)

    /**
      * Creates a [[SubscriptionName]] by parsing the full name
      * (projects/{project}/subscriptions/{subscription}) string.
      *
      * @param fullName the full name string
      * @return the [[SubscriptionName]]
      * @throws IllegalArgumentException if the full name cannot be parsed
      */
    def apply(fullName: String): SubscriptionName = fullName match {
      case SubscriptionNamePattern(project, subscription) =>
        SubscriptionName(project, subscription)
      case _ =>
        throw new IllegalArgumentException(
          s"Full subscription name '$fullName' does not match pattern '$SubscriptionNamePattern'."
        )
    }

    /**
      * Creates a [[SubscriptionName]] from a [[ProjectName]] and a subscription name.
      *
      * @param projectName the [[ProjectName]]
      * @param name the subscription name
      * @return the [[SubscriptionName]]
      */
    def apply(projectName: ProjectName, name: String): SubscriptionName =
      new SubscriptionName(projectName, name)
  }

  /**
    * Class for a Google Cloud Pub/Sub subscription name.
    *
    * @param projectName the name of the project
    * @param name the name of the subscription
    */
  class SubscriptionName(val projectName: ProjectName, val name: String) {
    val fullName: String = s"${projectName.fullName}/subscriptions/$name"
  }
}
