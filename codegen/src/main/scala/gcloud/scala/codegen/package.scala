package gcloud.scala

import java.lang.reflect.{Method, Modifier}

package object codegen {
  implicit class StringUtils(val string: String) extends AnyVal {
    def firstToLower: String = string.head.toLower + string.tail

    def firstToUpper: String = string.head.toUpper + string.tail

    def asScalaClass: String =
      string
        .replace("$", ".")
        .replace("<", "[")
        .replace(">", "]")
        .replaceAll("^int$", "Int")
        .replaceAll("^boolean$", "Boolean")
  }

  implicit class MethodUtils(val method: Method) extends AnyVal {
    def isSetter: Boolean = method.getName.startsWith("set")

    def isGetter: Boolean = method.getName.startsWith("get")

    def isPublic: Boolean = Modifier.isPublic(method.getModifiers)
  }

}
