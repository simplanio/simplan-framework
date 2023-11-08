package com.intuit.data.simplan.common.files

import scala.util.Try

/** @author Abraham, Thomas - tabraham1
  *         Created on 15-Nov-2021 at 10:16 AM
  */
trait FileUtils extends Serializable {
  val schemes: List[String] = List.empty

  def readContent(path: String, charset: String = "UTF-8"): String
  def readContentSafely(path: String, charset: String = "UTF-8"): Try[String] = Try(readContent(path, charset))
  def exists(path: String): Boolean
  def copy(sourcePath: String, destinationPath: String): Boolean
  def list(path: String, recursive: Boolean = true, filter: FileListing => Boolean = _ => true): List[FileListing]
  def writeContent(path: String, content: String): Boolean
  def getCountAndSize(path: String): (Long, Long)
}
