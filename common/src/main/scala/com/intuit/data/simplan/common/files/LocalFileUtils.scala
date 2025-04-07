package com.intuit.data.simplan.common.files

import org.apache.commons.io.FileUtils

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.Date
import scala.util.{Failure, Success, Try}

/** @author Abraham, Thomas - tabraham1
 *          Created on 15-Nov-2021 at 10:17 AM
 */
class LocalFileUtils extends FileUtils {
  override def readContent(path: String, charset: String = "UTF-8"): String = org.apache.commons.io.FileUtils.readFileToString(new File(path), charset)

  override def exists(path: String): Boolean = new File(path).exists()

  override def list(path: String, recursive: Boolean = true, filter: FileListing => Boolean = _ => true): List[FileListing] = new File(path).listFiles()
    .map(each => FileListing(each.getAbsolutePath, each.length(), new Date(each.lastModified())))
    .toList

  override def copy(sourcePath: String, destinationPath: String): Boolean = Try(FileUtils.copyDirectory(new File(sourcePath), new File(destinationPath))) match {
    case Success(_) => true
    case Failure(exception) => throw exception
  }


  override def writeContent(path: String, content: String): Boolean = Try(Files.write(Paths.get(path), content.getBytes(StandardCharsets.UTF_8))).isSuccess

  override def getCountAndSize(path: String): (Long, Long) = ???

  override val schemes: List[String] = List("file")
}
