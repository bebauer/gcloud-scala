package gcloud.scala.pubsub

import com.google.pubsub.v1
import org.scalatest.{Matchers, WordSpec}

class ProjectNameSpec extends WordSpec with Matchers {

  "ProjectName" should {
    "be created from name" in {
      val pn = ProjectName("test")

      pn.getProject shouldBe "test"
    }

    "be created from full name" in {
      val pn = ProjectName("projects/test")

      pn.getProject shouldBe "test"
    }

    "fail on empty name" in {
      intercept[IllegalArgumentException] { ProjectName("") }.getMessage shouldBe "Project name '' does not match pattern '(projects/)?(.+)'."
    }

    "convert implicitly from string" in {
      val pn: v1.ProjectName = "test"

      pn.getProject shouldBe "test"
    }
  }
}
