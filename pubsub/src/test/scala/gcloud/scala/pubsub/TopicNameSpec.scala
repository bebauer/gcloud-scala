package gcloud.scala.pubsub

import com.google.pubsub.v1
import org.scalatest.{Matchers, WordSpec}

class TopicNameSpec extends WordSpec with Matchers {

  "TopicName" should {

    "be created from full name" in {
      val tn = TopicName("projects/tp/topics/tt")

      tn.getProject shouldBe "tp"
      tn.getTopic shouldBe "tt"
    }

    "be created implicitly from string" in {
      val tn: v1.TopicName = "projects/tp/topics/tt"

      tn.getProject shouldBe "tp"
      tn.getTopic shouldBe "tt"
    }

    "fail on invalid name" in {
      intercept[IllegalArgumentException] { TopicName("projecs/tp/top/tt") }.getMessage shouldBe "Full topic name 'projecs/tp/top/tt' does not match pattern 'projects/(.+)/topics/(.+)'."
    }

    "be created from project and subscription names" in {
      val tn = TopicName(ProjectName("tp"), "tt")

      tn.getProject shouldBe "tp"
      tn.getTopic shouldBe "tt"
    }
  }
}
