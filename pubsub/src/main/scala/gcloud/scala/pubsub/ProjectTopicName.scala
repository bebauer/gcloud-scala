package gcloud.scala.pubsub

import com.google.pubsub.v1

object ProjectTopicName {
  private val TopicNamePattern = "projects/(.+)/topics/(.+)".r

  /**
    * Creates a [[v1.ProjectTopicName]] by parsing the full name (projects/{project}/topics/{topic}) string.
    *
    * @param fullName the full name string
    * @return the [[v1.ProjectTopicName]]
    * @throws IllegalArgumentException if the full name cannot be parsed
    */
  def apply(fullName: String): v1.ProjectTopicName = fullName match {
    case TopicNamePattern(project, topic) => ProjectTopicName(project, topic)
    case _ =>
      throw new IllegalArgumentException(
        s"Full topic name '$fullName' does not match pattern '$TopicNamePattern'."
      )
  }

  /**
    * Creates a [[v1.ProjectTopicName]] from a [[ProjectName]] and a topic name.
    *
    * @param projectName the [[ProjectName]]
    * @param name the topic name
    * @return the [[v1.ProjectTopicName]]
    */
  def apply(projectName: v1.ProjectName, name: String): v1.ProjectTopicName =
    v1.ProjectTopicName.of(projectName.getProject, name)
}
