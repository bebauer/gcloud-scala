package gcloud.scala.codegen

import java.lang.reflect.ParameterizedType

import com.google.api.gax.rpc.UnaryCallable
import treehugger.forest._
import definitions._
import gcloud.scala.codegen.Builder._
import treehugger.forest
import treehuggerDSL._

object Stubs {

  def generateStubExtensions(stubClass: Class[_],
                             flattenExcluded: Seq[Class[_]] = Seq()): forest.ClassDef = {
    val StubEnhanced = RootClass.newClass(s"${stubClass.getSimpleName}Extensions")
    val stub         = "stub"

    CLASSDEF(StubEnhanced) withParams VAL(stub, stubClass.getName).tree withFlags Flags.IMPLICIT withParents AnyValClass := BLOCK(
      stubClass.getMethods.collect {
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
            ).tree := REF(stub) DOT method.getName DOT "futureCall" APPLY REF("request") DOT "asScala"
          )

          flattenExcluded.find(_ == Class.forName(requestType)) match {
            case None =>
              defs :+ (DEF(method.getName.replace("Callable", "Async"), s"Future[$responseType]") withParams paramsFromBuilder(
                builderClass,
                PubSub.RequestIgnores,
                s"$requestObject.defaultBuilder"
              ) := REF(
                stub
              ) DOT method.getName DOT "futureCall" APPLY (REF(requestObject) APPLY builderFields(
                builderClass,
                PubSub.RequestIgnores
              ).map(bf => REF(bf.name) := REF(bf.name))) DOT "asScala")
            case Some(_) =>
              defs
          }
      }.flatten
    )
  }

  def generateStubObject(stubClass: Class[_],
                         settingsClass: Class[_],
                         settingsBuilderClass: Class[_],
                         ignoredBuilderMethods: Seq[String] = Seq()): forest.ModuleDef =
    OBJECTDEF(stubClass.getSimpleName) := BLOCK(
      (DEF("apply", stubClass.getName) withParams PARAM("url", "PubSubUrl").tree := REF(
        stubClass.getSimpleName
      ) APPLY (REF("url") withType "Settings")) +:
      (DEF("apply", stubClass.getName) withParams PARAM(
        "settings",
        settingsClass.getName.asScalaClass
      ).tree := REF(
        "settings"
      ) DOT "createStub" APPLY ()) +:
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
