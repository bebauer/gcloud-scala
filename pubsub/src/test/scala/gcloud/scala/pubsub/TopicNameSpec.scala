package gcloud.scala.pubsub

import org.scalatest.{Matchers, WordSpec}

class TopicNameSpec extends WordSpec with Matchers {

  "TopicName" should {

    "be created from full name" in {
      val tn = TopicName("projects/tp/topics/tt")

      tn.projectName.name shouldBe "tp"
      tn.name shouldBe "tt"
    }

    "be created implicitly from string" in {
      val tn: TopicName = "projects/tp/topics/tt"

      tn.projectName.name shouldBe "tp"
      tn.name shouldBe "tt"
    }

    "fail on invalid name" in {
      intercept[IllegalArgumentException] { TopicName("projecs/tp/top/tt") }.getMessage shouldBe "Full topic name 'projecs/tp/top/tt' does not match pattern 'projects/(.+)/topics/(.+)'."
    }

    "be created from project and subscription names" in {
      val tn = TopicName(ProjectName("tp"), "tt")

      tn.projectName.name shouldBe "tp"
      tn.name shouldBe "tt"
    }
  }
}
