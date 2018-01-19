package gcloud.scala.pubsub

import com.google.pubsub.v1

object ProjectName {
  private val ProjectNamePattern = "(projects/)?(.+)".r

  /**
    * Creates a [[v1.ProjectName]] by parsing a name string.
    * The name can be specified with or without the 'projects/' prefix.
    *
    * @param name the project name
    * @return the [[v1.ProjectName]]
    * @throws IllegalArgumentException if the name cannot be parsed
    */
  def apply(name: String): v1.ProjectName =
    v1.ProjectName
      .newBuilder()
      .setProject(name match {
        case ProjectNamePattern(n)    => n
        case ProjectNamePattern(_, n) => n
        case _ =>
          throw new IllegalArgumentException(
            s"Project name '$name' does not match pattern '$ProjectNamePattern'."
          )
      })
      .build()
}
