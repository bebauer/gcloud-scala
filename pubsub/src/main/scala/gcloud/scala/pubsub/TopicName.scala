package gcloud.scala.pubsub

import com.google.pubsub.v1

object TopicName {
  private val TopicNamePattern = "projects/(.+)/topics/(.+)".r

  /**
    * Creates a [[TopicName]] by parsing the full name (projects/{project}/topics/{topic}) string.
    *
    * @param fullName the full name string
    * @return the [[TopicName]]
    * @throws IllegalArgumentException if the full name cannot be parsed
    */
  def apply(fullName: String): v1.TopicName = fullName match {
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
  def apply(projectName: v1.ProjectName, name: String): v1.TopicName =
    v1.TopicName.newBuilder().setProject(projectName.getProject).setTopic(name).build()
}
