package gcloud.scala.codegen

import java.util.regex.Pattern

import com.google.cloud.pubsub.v1.stub.{
  PublisherStub,
  PublisherStubSettings,
  SubscriberStub,
  SubscriberStubSettings
}
import com.google.cloud.pubsub.v1.{
  SubscriptionAdminClient,
  SubscriptionAdminSettings,
  TopicAdminClient,
  TopicAdminSettings
}
import com.google.iam.v1.{GetIamPolicyRequest, SetIamPolicyRequest, TestIamPermissionsRequest}
import com.google.pubsub.v1._
import gcloud.scala.codegen.Builder.generateCompanion
import gcloud.scala.codegen.Clients._
import gcloud.scala.codegen.Stubs._
import gcloud.scala.codegen.Response._
import treehugger.forest._
import treehuggerDSL._

import scala.language.postfixOps

object PubSub {
  final val RequestIgnores = Seq(
    "clone",
    "clear",
    Pattern.compile(".*Field.*"),
    "mergeFrom",
    "clearOneof",
    Pattern.compile("set.*Bytes"),
    Pattern.compile("set.*With.*Name")
  )

  final val ResponseIgnores = Seq(
    Pattern.compile("get.*OrBuilder.*")
  )

  def generate(): String =
    replaceTemplates(
      treeToString(
        BLOCK(
          IMPORT("scala.collection.JavaConverters._"),
          PACKAGE("gcloud.scala.pubsub") := BLOCK(
            PACKAGEOBJECTDEF("syntax") := BLOCK(
              IMPORT("scala.concurrent.Future"),
              IMPORT("gcloud.scala.pubsub.FutureConversions._"),
              generateClientExtensions(classOf[SubscriptionAdminClient],
                                       Seq(classOf[Subscription]),
                                       additionalSubscriptionAdminDefs),
              generateClientExtensions(classOf[TopicAdminClient],
                                       Seq(classOf[Topic]),
                                       additionalTopicAdminDefs),
              generateStubExtensions(classOf[PublisherStub], Seq(classOf[Topic])),
              generateStubExtensions(classOf[SubscriberStub], Seq(classOf[Subscription])),
              generateResponseSyntax(classOf[PullResponse], ResponseIgnores)
            ),
            generateStubObject(classOf[PublisherStub],
                               classOf[PublisherStubSettings],
                               classOf[PublisherStubSettings.Builder]),
            generateStubObject(classOf[SubscriberStub],
                               classOf[SubscriberStubSettings],
                               classOf[SubscriberStubSettings.Builder]),
            generateClientObject(classOf[SubscriptionAdminClient],
                                 classOf[SubscriptionAdminSettings],
                                 classOf[SubscriptionAdminSettings.Builder],
                                 Seq("getStubSettingsBuilder")),
            generateClientObject(classOf[TopicAdminClient],
                                 classOf[TopicAdminSettings],
                                 classOf[TopicAdminSettings.Builder],
                                 Seq("getStubSettingsBuilder")),
            generateCompanion(classOf[GetSubscriptionRequest],
                              classOf[GetSubscriptionRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[UpdateSubscriptionRequest],
                              classOf[UpdateSubscriptionRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[ListSubscriptionsRequest],
                              classOf[ListSubscriptionsRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[DeleteSubscriptionRequest],
                              classOf[DeleteSubscriptionRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[SetIamPolicyRequest],
                              classOf[SetIamPolicyRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[GetIamPolicyRequest],
                              classOf[GetIamPolicyRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[CreateSnapshotRequest],
                              classOf[CreateSnapshotRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[UpdateSnapshotRequest],
                              classOf[UpdateSnapshotRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[DeleteSnapshotRequest],
                              classOf[DeleteSnapshotRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[SeekRequest], classOf[SeekRequest.Builder], RequestIgnores),
            generateCompanion(classOf[ModifyPushConfigRequest],
                              classOf[ModifyPushConfigRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[ListSnapshotsRequest],
                              classOf[ListSnapshotsRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[TestIamPermissionsRequest],
                              classOf[TestIamPermissionsRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[GetTopicRequest],
                              classOf[GetTopicRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[ListTopicsRequest],
                              classOf[ListTopicsRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[ListTopicSubscriptionsRequest],
                              classOf[ListTopicSubscriptionsRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[DeleteTopicRequest],
                              classOf[DeleteTopicRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[UpdateTopicRequest],
                              classOf[UpdateTopicRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[PublishRequest],
                              classOf[PublishRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[ModifyAckDeadlineRequest],
                              classOf[ModifyAckDeadlineRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[AcknowledgeRequest],
                              classOf[AcknowledgeRequest.Builder],
                              RequestIgnores),
            generateCompanion(classOf[PullRequest], classOf[PullRequest.Builder], RequestIgnores)
          )
        ) withoutPackage
      ),
      "val getTopicOptionAsync"        -> txt.TopicAdminClientGen().toString(),
      "val getSubscriptionOptionAsync" -> txt.SubscriptionAdminClientGen().toString()
    )

  private def replaceTemplates(text: String, replacements: (String, String)*) =
    replacements.foldLeft(text) { (text, replacement) =>
      text.replace(replacement._1, replacement._2)
    }
}
