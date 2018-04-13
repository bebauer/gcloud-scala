package gcloud.scala.pubsub

import com.google.pubsub.v1

/**
  * Companion object for [[v1.ProjectSubscriptionName]].
  */
object ProjectSubscriptionName {
  private val SubscriptionNamePattern = "projects/(.+)/subscriptions/(.+)".r

  /**
    * Creates a [[v1.ProjectSubscriptionName]] by parsing the full name
    * (projects/{project}/subscriptions/{subscription}) string.
    *
    * @param fullName the full name string
    * @return the [[v1.ProjectSubscriptionName]]
    * @throws IllegalArgumentException if the full name cannot be parsed
    */
  def apply(fullName: String): v1.ProjectSubscriptionName = fullName match {
    case SubscriptionNamePattern(project, subscription) =>
      ProjectSubscriptionName(project, subscription)
    case _ =>
      throw new IllegalArgumentException(
        s"Full subscription name '$fullName' does not match pattern '$SubscriptionNamePattern'."
      )
  }

  /**
    * Creates a [[v1.ProjectSubscriptionName]] from a [[v1.ProjectName]] and a subscription name.
    *
    * @param projectName the [[v1.ProjectName]]
    * @param subscriptionName the subscription name
    * @return the [[v1.ProjectSubscriptionName]]
    */
  def apply(projectName: v1.ProjectName, subscriptionName: String): v1.ProjectSubscriptionName =
    v1.ProjectSubscriptionName.of(projectName.getProject, subscriptionName)
}
