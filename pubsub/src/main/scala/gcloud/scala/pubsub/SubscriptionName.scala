package gcloud.scala.pubsub

import com.google.pubsub.v1

object SubscriptionName {
  private val SubscriptionNamePattern = "projects/(.+)/subscriptions/(.+)".r

  /**
    * Creates a [[v1.SubscriptionName]] by parsing the full name
    * (projects/{project}/subscriptions/{subscription}) string.
    *
    * @param fullName the full name string
    * @return the [[v1.SubscriptionName]]
    * @throws IllegalArgumentException if the full name cannot be parsed
    */
  def apply(fullName: String): v1.SubscriptionName = fullName match {
    case SubscriptionNamePattern(project, subscription) =>
      SubscriptionName(project, subscription)
    case _ =>
      throw new IllegalArgumentException(
        s"Full subscription name '$fullName' does not match pattern '$SubscriptionNamePattern'."
      )
  }

  /**
    * Creates a [[v1.SubscriptionName]] from a [[v1.ProjectName]] and a subscription name.
    *
    * @param projectName the [[v1.ProjectName]]
    * @param subscriptionName the subscription name
    * @return the [[v1.SubscriptionName]]
    */
  def apply(projectName: v1.ProjectName, subscriptionName: String): v1.SubscriptionName =
    v1.SubscriptionName
      .newBuilder()
      .setProject(projectName.getProject)
      .setSubscription(subscriptionName)
      .build()
}
