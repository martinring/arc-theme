import java.io.{File, FileOutputStream, FileWriter, OutputStreamWriter}
import java.nio.charset.StandardCharsets

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.Try

/**
  * Created by martin on 06.12.16.
  */

val wd = new File(".")

val factor = Try(args(0)).map(_.toDouble).getOrElse {
  println("usage: 'scala resize.scala [factor]' factor: Double")
  sys.exit(-1)
}


def getScssFiles(dir: File): Seq[File] = dir.listFiles().flatMap {
  case dir if dir.isDirectory =>
    getScssFiles(dir)
  case f if f.getName.endsWith(".scss") =>
    Seq(f)
  case other => Seq.empty
}

val scssFiles = getScssFiles(wd)

val total = scssFiles.size

val regex = "([0-9]+)\\s*(cm|mm|in|px|pt|pc)".r

scssFiles.zipWithIndex.foreach {
  case (file,i) =>
    val index = i + 1
    var modified = false
    println(s"# processing file $index of $total: ${file.getName}")

    val origin = new File(file.getAbsolutePath + "_origin")
    if (!origin.exists())
      file.renameTo(origin)

    val writer =
      new FileWriter(file,false)

    Source.fromFile(origin).getLines().foreach { line =>
      var remaining = line
      var replaced = ""
      var m = regex.findFirstMatchIn(remaining)
      while (remaining.length > 0 && m.isDefined) {
        modified = true
        val n = m.get.group(1).toDouble
        val u = m.get.group(2)
        val scaled = (n * factor).round.toInt.toString + u
        println(m.get.matched + " -> " + scaled)
        replaced += remaining.take(m.get.start)
        replaced += scaled
        remaining = remaining.drop(m.get.end)
        m = regex.findFirstMatchIn(remaining)
      }
      replaced += remaining
      writer.append(replaced + "\n")
    }

    writer.flush()
    writer.close()

    if (!modified) origin.delete()
}