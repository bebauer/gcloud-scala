package gcloud.scala.codegen

import java.lang.reflect.ParameterizedType

import com.google.api.gax.rpc.UnaryCallable
import gcloud.scala.codegen.Builder._
import treehugger.forest
import treehugger.forest._
import definitions._
import treehuggerDSL._

object Clients {
  def generateClientExtensions(clientClass: Class[_]): forest.ClassDef = {
    val ClientEnhanced = RootClass.newClass(s"${clientClass.getSimpleName}Extensions")
    val client         = "client"

    CLASSDEF(ClientEnhanced) withParams VAL(client, clientClass.getName).tree withFlags Flags.IMPLICIT withParents AnyValClass := BLOCK(
      clientClass.getMethods.collect {
        case method if method.getReturnType == classOf[UnaryCallable[_, _]] =>
          val requestType =
            method.getGenericReturnType
              .asInstanceOf[ParameterizedType]
              .getActualTypeArguments()(0)
              .getTypeName
              .asScalaClass
          val responseType =
            method.getGenericReturnType
              .asInstanceOf[ParameterizedType]
              .getActualTypeArguments()(1)
              .getTypeName
              .asScalaClass

          DEF(method.getName.replace("Callable", "Async"), s"Future[$responseType]") withParams PARAM(
            "request",
            requestType
          ).tree := REF(client) DOT method.getName DOT "futureCall" APPLY REF("request") DOT "asScala"
      }
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
      (DEF("apply", clientClass.getName) withParams PARAM(
        "settings",
        settingsClass.getName.asScalaClass
      ).tree := REF(clientClass.getName) DOT "create" APPLY REF("settings")) +:
      generateBuilder(settingsClass, settingsBuilderClass, ignoredBuilderMethods) :+
      (DEF(
        "pubsubUrlToSettings",
        "Settings"
      ) withFlags Flags.IMPLICIT withParams PARAM(
        "url",
        "PubSubUrl"
      ).tree := REF("Settings") APPLY (REF("transportChannelProvider") := REF("url") DOT "channelProviderBuilder" APPLY () DOT "build" APPLY ())): _*
    )
}
