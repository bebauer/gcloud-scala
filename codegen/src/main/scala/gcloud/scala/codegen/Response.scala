package gcloud.scala.codegen

import java.lang.reflect.{Method, ParameterizedType}
import java.util.regex.Pattern

import treehugger.forest
import treehugger.forest._
import treehuggerDSL._
import definitions._

object Response {
  def generateResponseSyntax(responseClass: Class[_], ignoreMethods: Seq[Any]): forest.ClassDef = {
    val ResponseEnhanced = RootClass.newClass(s"${responseClass.getSimpleName}Extensions")
    val response         = "response"

    CLASSDEF(ResponseEnhanced) withParams VAL(response, responseClass.getName).tree withFlags Flags.IMPLICIT withParents AnyValClass := BLOCK(
      responseClass.getMethods
        .groupBy(_.getName)
        .mapValues(_.head)
        .values
        .collect {
          case method
              if isNotIgnored(ignoreMethods, method) && method.getName
                .startsWith("get") && method.getName
                .endsWith("List") && method.getGenericReturnType
                .isInstanceOf[ParameterizedType] =>
            val getterType = method.getGenericReturnType.asInstanceOf[ParameterizedType]
            val typeName =
              s"Seq[${getterType.getActualTypeArguments.map(_.getTypeName).mkString(",")}]"
            DEF(method.getName.substring(3, method.getName.length - 4).firstToLower, typeName) := REF(
              response
            ) DOT method.getName DOT "asScala"
        }
    )
  }

  private def isNotIgnored(ignoreMethods: Seq[Any], method: Method) =
    !ignoreMethods.exists {
      case pattern: Pattern => pattern.matcher(method.getName).matches()
      case other            => other.toString == method.getName
    }
}
