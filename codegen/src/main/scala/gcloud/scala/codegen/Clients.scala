package gcloud.scala.codegen

import java.lang.reflect.ParameterizedType

import com.google.api.gax.rpc.UnaryCallable
import com.google.pubsub.v1.{Subscription, Topic}
import gcloud.scala.codegen.Builder._
import treehugger.forest
import treehugger.forest._
import definitions._
import treehuggerDSL._

object Clients {
  def generateClientExtensions(clientClass: Class[_],
                               flattenExcluded: Seq[Class[_]] = Seq(),
                               customDefs: Seq[Tree] = Seq()): forest.ClassDef = {
    val ClientEnhanced = RootClass.newClass(s"${clientClass.getSimpleName}Extensions")
    val client         = "client"

    CLASSDEF(ClientEnhanced) withParams VAL(client, clientClass.getName).tree withFlags Flags.IMPLICIT withParents AnyValClass := BLOCK(
      clientClass.getMethods.collect {
        case method if method.getReturnType == classOf[UnaryCallable[_, _]] =>
          val requestObject = method.getGenericReturnType
            .asInstanceOf[ParameterizedType]
            .getActualTypeArguments()(0)
            .asInstanceOf[Class[_]]
            .getSimpleName
          val requestType =
            method.getGenericReturnType
              .asInstanceOf[ParameterizedType]
              .getActualTypeArguments()(0)
              .getTypeName
          val responseType =
            method.getGenericReturnType
              .asInstanceOf[ParameterizedType]
              .getActualTypeArguments()(1)
              .getTypeName
              .asScalaClass

          val builderClass = Class.forName(s"$requestType$$Builder")

          val defs = Seq(
            DEF(method.getName.replace("Callable", "Async"), s"Future[$responseType]") withParams PARAM(
              "request",
              requestType.asScalaClass
            ).tree := REF(client) DOT method.getName DOT "futureCall" APPLY REF("request") DOT "asScala"
          )

          flattenExcluded.find(_ == Class.forName(requestType)) match {
            case None =>
              defs :+ (DEF(method.getName.replace("Callable", "Async"), s"Future[$responseType]") withParams paramsFromBuilder(
                builderClass,
                PubSub.RequestIgnores,
                s"$requestObject.defaultBuilder"
              ) := REF(
                client
              ) DOT method.getName DOT "futureCall" APPLY (REF(requestObject) APPLY builderFields(
                builderClass,
                PubSub.RequestIgnores
              ).map(bf => REF(bf.name) := REF(bf.name))) DOT "asScala")
            case Some(_) =>
              defs
          }
      }.flatten ++ customDefs
    )
  }

  def generateClientObject(clientClass: Class[_],
                           settingsClass: Class[_],
                           settingsBuilderClass: Class[_],
                           ignoredBuilderMethods: Seq[String] = Seq()): forest.ModuleDef =
    OBJECTDEF(clientClass.getSimpleName) := BLOCK(
      (DEF("apply", clientClass.getName) withParams PARAM("url", "PubSubUrl").tree := REF(
        clientClass.getSimpleName
      ) APPLY (REF("url") withType "Settings")) +:
      (DEF("apply", clientClass.getName) withParams (PARAM(
        "settings",
        settingsClass.getName.asScalaClass
      ) := (REF("Settings") APPLY ())) := REF(clientClass.getName) DOT "create" APPLY REF(
        "settings"
      )) +:
      (OBJECTDEF("Settings") := BLOCK(
        DEF("apply", "Settings") withParams PARAM("url", "PubSubUrl").tree := REF("url")
      )) +:
      generateBuilder(settingsClass, settingsBuilderClass, ignoredBuilderMethods) :+
      (DEF(
        "pubsubUrlToSettings",
        "Settings"
      ) withFlags Flags.IMPLICIT withParams PARAM(
        "url",
        "PubSubUrl"
      ).tree := REF("Settings") APPLY (REF("transportChannelProvider") := REF("url") DOT "channelProviderBuilder" APPLY () DOT "build" APPLY ())): _*
    )

  def additionalTopicAdminDefs: Seq[Tree] = Seq(VAL("getTopicOptionAsync").tree)

  def additionalSubscriptionAdminDefs: Seq[Tree] = Seq(VAL("getSubscriptionOptionAsync").tree)
}
