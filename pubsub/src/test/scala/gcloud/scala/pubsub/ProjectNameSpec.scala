package gcloud.scala.pubsub

import org.scalatest.{Matchers, WordSpec}

class ProjectNameSpec extends WordSpec with Matchers {

  "ProjectName" should {
    "be created from name" in {
      val pn = ProjectName("test")

      pn.name shouldBe "test"
    }

    "be created from full name" in {
      val pn = ProjectName("projects/test")

      pn.name shouldBe "test"
    }

    "fail on empty name" in {
      intercept[IllegalArgumentException] { ProjectName("") }.getMessage shouldBe "Project name '' does not match pattern '(projects/)?(.+)'."
    }

    "convert implicitly from string" in {
      val pn: ProjectName = "test"

      pn.name shouldBe "test"
    }
  }
}
