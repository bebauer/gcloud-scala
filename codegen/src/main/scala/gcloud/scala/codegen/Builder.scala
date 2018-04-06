package gcloud.scala.codegen

import java.lang.reflect.{Method, Modifier, ParameterizedType}
import java.util.regex.Pattern

import treehugger.forest
import treehugger.forest._
import treehuggerDSL._

object Builder {

  def generateBuilder(targetClass: Class[_],
                      builderClass: Class[_],
                      ignoreMethods: Seq[String] = Seq()): Seq[Tree] = {
    val dataClassName = "Settings"

    Seq(
      // private val for default builder
      VAL("defaultBuilder") withFlags Flags.PRIVATE := REF(targetClass.getName) DOT "newBuilder" APPLY (),
      // builder case class
      CASECLASSDEF(dataClassName) withParams paramsFromBuilder(builderClass, ignoreMethods) := EmptyTree,
      // implicit conversion from case class to google builder
      DEF("caseClassToBuilder", builderClass.getName.asScalaClass) withFlags Flags.IMPLICIT withParams PARAM(
        "settings",
        dataClassName
      ).tree := BLOCK(
        (VAL("builder", builderClass.getName.asScalaClass) := REF(targetClass.getName) DOT "newBuilder" APPLY ()) +:
        fillBuilderFromCaseClass(builderClass) :+ REF("builder"): _*
      ),
      // implicit conversion from settings case class to the target instance
      DEF("caseClassToSettings", targetClass.getName.asScalaClass) withFlags Flags.IMPLICIT withParams PARAM(
        "settings",
        dataClassName
      ).tree := REF("settings") DOT "build" APPLY ()
    )
  }

  def generateCompanion(targetClass: Class[_],
                        builderClass: Class[_],
                        ignoreMethods: Seq[Any] = Seq()): forest.ModuleDef =
    OBJECTDEF(targetClass.getSimpleName) := BLOCK(
      // private val for default builder
      VAL("defaultBuilder") withFlags PRIVATEWITHIN("pubsub") := REF(
        targetClass.getName
      ) DOT "newBuilder" APPLY (),
      // apply method to construct instance through builder
      DEF("apply", targetClass.getName.asScalaClass) withParams paramsFromBuilder(
        builderClass,
        ignoreMethods
      ) := BLOCK(
        (VAL("builder", builderClass.getName.asScalaClass) := REF(targetClass.getName) DOT "newBuilder" APPLY ()) +:
        paramsFromBuilder(builderClass, ignoreMethods)
          .map {
            case param if param.tpe.toString().contains("Seq[") =>
              REF("builder") DOT s"addAll${param.name.name.firstToUpper}" APPLY (REF(
                param.name.name
              ) DOT "asJava")
            case param =>
              REF("builder") DOT s"set${param.name.name.firstToUpper}" APPLY REF(param.name.name)
          } :+ (REF("builder") DOT "build" APPLY ())
      )
    )

  def paramsFromBuilder(builderClass: Class[_],
                        ignoreMethods: Seq[Any],
                        defaultBuilder: String = "defaultBuilder"): Iterable[forest.ValDef] =
    builderFields(builderClass, ignoreMethods).map {
      case BuilderField(name, returnType, Some(DefaultGetter(defaultName, false))) =>
        PARAM(name, returnType) := REF(defaultBuilder) DOT defaultName APPLY ()
      case BuilderField(name, returnType, Some(DefaultGetter(defaultName, true))) =>
        PARAM(name, returnType) := REF(defaultBuilder) DOT defaultName APPLY () DOT "asScala"
      case BuilderField(name, returnType, None) =>
        PARAM(name, returnType).tree
    }

  case class BuilderField(name: String,
                          returnType: String,
                          defaultGetter: Option[DefaultGetter] = None)

  case class DefaultGetter(name: String, convert: Boolean = false)

  def builderFields(builderClass: Class[_], ignoreMethods: Seq[Any]): Iterable[BuilderField] =
    builderClass.getMethods
      .groupBy(_.getName)
      .mapValues(_.head)
      .values
      .collect {
        case method
            if isNotIgnored(ignoreMethods, method) && method.isPublic && method.isSetter && method.getReturnType
              .isAssignableFrom(builderClass) && method.getParameterCount == 1 =>
          val getter = method.getName.replace("set", "get")
          if (builderClass.getMethods.exists(_.getName == getter)) {
            val getterType = builderClass.getMethod(getter).getReturnType

            BuilderField(getter.substring(3).firstToLower,
                         getterType.getTypeName.asScalaClass,
                         Some(DefaultGetter(getter)))
          } else {
            BuilderField(getter.substring(3).firstToLower,
                         method.getParameterTypes()(0).getTypeName.asScalaClass)
          }
        case method
            if isNotIgnored(ignoreMethods, method) && method.isPublic && method.isSetter && method.getReturnType
              .isAssignableFrom(builderClass) && method.getParameterCount == 2 =>
          val getter = s"${method.getName.replace("set", "get")}List"
          val paramType =
            method.getParameterTypes()(1).getTypeName.asScalaClass.replaceAll("\\.Builder$", "")

          if (builderClass.getMethods.exists(_.getName == getter)) {
            BuilderField(method.getName.substring(3).firstToLower,
                         s"Seq[$paramType]",
                         Some(DefaultGetter(getter, convert = true)))
          } else {
            BuilderField(method.getName.substring(3).firstToLower, s"Seq[$paramType]")
          }
        case method
            if isNotIgnored(ignoreMethods, method) && method.isPublic && method.getReturnType != builderClass && method.getReturnType.getName
              .endsWith("$Builder") && method.getGenericReturnType
              .isInstanceOf[ParameterizedType] =>
          println(method)
          val getterType = method.getGenericReturnType.asInstanceOf[ParameterizedType]
          val typeName   = getterType.getRawType.getTypeName + s"[${getterType.getActualTypeArguments.map(_.getTypeName).mkString(",")}]"
          BuilderField(method.getName, typeName.asScalaClass, Some(DefaultGetter(method.getName)))
        case method
            if isNotIgnored(ignoreMethods, method) && method.isPublic && method.getReturnType != builderClass && method.getReturnType.getName
              .endsWith("$Builder") =>
          println(method)
          BuilderField(method.getName,
                       method.getReturnType.getName.asScalaClass,
                       Some(DefaultGetter(method.getName)))
      }
      .filter { bf =>
        builderClass.getMethods.map(_.getName).contains(s"set${bf.name.firstToUpper}")
      }
      .toSeq
      .sortBy(_.name)

  private def createListParam(builderClass: Class[_], method: Method) = {
    val getter = s"${method.getName.replace("set", "get")}List"
    val paramType =
      method.getParameterTypes()(1).getTypeName.asScalaClass.replaceAll("\\.Builder$", "")

    if (builderClass.getMethods.exists(_.getName == getter)) {
      PARAM(method.getName.substring(3).firstToLower, s"Seq[$paramType]") := REF(
        "defaultBuilder"
      ) DOT getter APPLY () DOT "asScala"
    } else {
      PARAM(method.getName.substring(3).firstToLower, s"Seq[$paramType]").tree
    }
  }

  private def fillBuilderFromCaseClass(builderClass: Class[_]) = builderClass.getMethods.collect {
    case method
        if Modifier.isPublic(method.getModifiers) && method.getName
          .startsWith("set") && method.getReturnType.isAssignableFrom(builderClass) =>
      REF("builder") DOT method.getName APPLY (REF("settings") DOT method.getName
        .substring(3)
        .firstToLower)
  }

  private def isNotIgnored(ignoreMethods: Seq[Any], method: Method) =
    !ignoreMethods.exists {
      case pattern: Pattern => pattern.matcher(method.getName).matches()
      case other            => other.toString == method.getName
    }
}
