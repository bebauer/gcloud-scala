package gcloud.scala.codegen

import java.io.File
import java.nio.file.Files

import scala.language.postfixOps

object Main extends App {

  args.toList match {
    case path :: Nil if path.endsWith(".scala") =>
      val jfile = new File(path)
      jfile.getParentFile.mkdirs()

      Files.write(jfile.toPath, PubSub.generate().getBytes("UTF-8"))
    case _ =>
      println(PubSub.generate())
  }
}
