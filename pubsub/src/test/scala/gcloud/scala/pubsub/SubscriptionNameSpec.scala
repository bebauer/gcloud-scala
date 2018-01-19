package gcloud.scala.pubsub

import com.google.pubsub.v1
import org.scalatest.{Matchers, WordSpec}

class SubscriptionNameSpec extends WordSpec with Matchers {

  "SubscriptionName" should {

    "be created from full name" in {
      val sn = SubscriptionName("projects/tp/subscriptions/ts")

      sn.getProject shouldBe "tp"
      sn.getSubscription shouldBe "ts"
    }

    "be created implicitly from string" in {
      val sn: v1.SubscriptionName = "projects/tp/subscriptions/ts"

      sn.getProject shouldBe "tp"
      sn.getSubscription shouldBe "ts"
    }

    "fail on invalid name" in {
      intercept[IllegalArgumentException] { SubscriptionName("projecs/tp/subsriptions/ts") }.getMessage shouldBe "Full subscription name 'projecs/tp/subsriptions/ts' does not match pattern 'projects/(.+)/subscriptions/(.+)'."
    }

    "be created from project and subscription names" in {
      val sn = SubscriptionName(ProjectName("tp"), "ts")

      sn.getProject shouldBe "tp"
      sn.getSubscription shouldBe "ts"
    }
  }
}
