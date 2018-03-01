package gcloud.scala.codegen

import java.io.File
import java.lang.reflect.{Modifier, ParameterizedType}
import java.nio.file.Files

import com.google.api.gax.rpc.UnaryCallable
import com.google.cloud.pubsub.v1.stub.{
  PublisherStub,
  PublisherStubSettings,
  SubscriberStub,
  SubscriberStubSettings
}
import treehugger.forest._
import definitions._
import treehuggerDSL._

import scala.language.postfixOps

object Main extends App {

  args.toList match {
    case path :: Nil if path.endsWith(".scala") =>
      val jfile = new File(path)
      jfile.getParentFile.mkdirs()

      Files.write(jfile.toPath, generate().getBytes("UTF-8"))
    case _ =>
      println(generate())
  }

  private def generate() = treeToString(
    BLOCK(
      PACKAGE("gcloud.scala.pubsub") := BLOCK(
        PACKAGEOBJECTDEF("stubs") := BLOCK(
          IMPORT("scala.concurrent.Future"),
          IMPORT("gcloud.scala.pubsub.FutureConversions._"),
          generateStubExtensions(classOf[PublisherStub]),
          generateStubExtensions(classOf[SubscriberStub])
        )
      ),
      PACKAGE("gcloud.scala.pubsub.stubs") := BLOCK(
        IMPORT("gcloud.scala.pubsub.PubSubUrl"),
        generateStubObject(classOf[PublisherStub],
                           classOf[PublisherStubSettings],
                           classOf[PublisherStubSettings.Builder]),
        generateStubObject(classOf[SubscriberStub],
                           classOf[SubscriberStubSettings],
                           classOf[SubscriberStubSettings.Builder])
      )
    ) withoutPackage
  )

  private def generateStubExtensions(stubClass: Class[_]) = {
    val StubEnhanced = RootClass.newClass(s"${stubClass.getSimpleName}Extensions")
    val stub         = "stub"

    CLASSDEF(StubEnhanced) withParams VAL(stub, stubClass.getName).tree withFlags Flags.IMPLICIT withParents AnyValClass := BLOCK(
      stubClass.getMethods.collect {
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

          DEF(method.getName.replace("Callable", ""), s"Future[$responseType]") withParams PARAM(
            "request",
            requestType
          ).tree := REF(stub) DOT method.getName DOT "futureCall" APPLY REF("request") DOT "asScala"
      }
    )
  }

  private def generateStubObject(stubClass: Class[_],
                                 settingsClass: Class[_],
                                 settingsBuilderClass: Class[_]) = {
    val StubSettings = s"${stubClass.getName}Settings"

    OBJECTDEF(stubClass.getSimpleName) := BLOCK(
      (DEF("apply", stubClass.getName) withParams PARAM("url", "PubSubUrl").tree := REF(
        stubClass.getSimpleName
      ) APPLY (REF("url") withType "Settings")) +:
      (DEF("apply", stubClass.getName) withParams PARAM("settings", StubSettings).tree := REF(
        "settings"
      ) DOT "createStub" APPLY ()) +:
      generateBuilder(settingsClass, settingsBuilderClass): _*
    )
  }

  private def generateBuilder(stubClass: Class[_], builderClass: Class[_]): Seq[Tree] = {
    val dataClassName = "Settings"

    Seq(
      VAL("defaultBuilder") withFlags Flags.PRIVATE := REF(stubClass.getName) DOT "newBuilder" APPLY (),
      CASECLASSDEF(dataClassName) withParams paramsFromBuilder(builderClass) := EmptyTree,
      DEF("caseClassToBuilder", builderClass.getName.asScalaClass) withFlags Flags.IMPLICIT withParams PARAM(
        "settings",
        dataClassName
      ).tree := BLOCK(
        (VAL("builder", builderClass.getName.asScalaClass) := REF(stubClass.getName) DOT "newBuilder" APPLY ()) +:
        fillBuilderFromCaseClass(builderClass) :+ REF("builder"): _*
      ),
      DEF("caseClassToSettings", stubClass.getName.asScalaClass) withFlags Flags.IMPLICIT withParams PARAM(
        "settings",
        dataClassName
      ).tree := REF("settings") DOT "build" APPLY (),
      DEF("pubsubUrlToSettings", dataClassName) withFlags Flags.IMPLICIT withParams PARAM(
        "url",
        "PubSubUrl"
      ).tree := REF(dataClassName) APPLY (REF("transportChannelProvider") := REF("url") DOT "channelProviderBuilder" APPLY () DOT "build" APPLY ())
    )
  }

  private def paramsFromBuilder(builderClass: Class[_]) = builderClass.getMethods.collect {
    case method
        if Modifier.isPublic(method.getModifiers) && method.getName
          .startsWith("set") && method.getReturnType.isAssignableFrom(builderClass) =>
      val getter     = method.getName.replace("set", "get")
      val getterType = builderClass.getMethod(getter).getReturnType

      PARAM(getter.substring(3).firstToLower, getterType.getTypeName) := REF("defaultBuilder") DOT getter APPLY ()
    case method
        if Modifier
          .isPublic(method.getModifiers) && method.getReturnType != builderClass && method.getReturnType.getName
          .endsWith("$Builder") =>
      val getterType = method.getGenericReturnType.asInstanceOf[ParameterizedType]
      val typeName   = getterType.getRawType.getTypeName + s"[${getterType.getActualTypeArguments.map(_.getTypeName).mkString(",")}]"
      PARAM(method.getName, typeName.asScalaClass) := REF("defaultBuilder") DOT method.getName APPLY ()
  }

  private def fillBuilderFromCaseClass(builderClass: Class[_]) = builderClass.getMethods.collect {
    case method
        if Modifier.isPublic(method.getModifiers) && method.getName
          .startsWith("set") && method.getReturnType.isAssignableFrom(builderClass) =>
      REF("builder") DOT method.getName APPLY (REF("settings") DOT method.getName
        .substring(3)
        .firstToLower)
  }

  implicit class StringUtils(val string: String) extends AnyVal {
    def firstToLower: String = string.head.toLower + string.tail

    def asScalaClass: String =
      string
        .replace("$", ".")
        .replace("<", "[")
        .replace(">", "]")
  }
}
