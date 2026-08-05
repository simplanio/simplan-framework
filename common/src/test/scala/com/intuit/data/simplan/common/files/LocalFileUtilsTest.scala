package com.intuit.data.simplan.common.files

import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import java.nio.file.Files

class LocalFileUtilsTest extends AnyFunSuite {

  val fileUtils = new LocalFileUtils

  test("schemes should contain 'file'") {
    assert(fileUtils.schemes == List("file"))
  }

  test("exists should return true for existing file") {
    val tmp = Files.createTempFile("simplan-test", ".txt").toFile
    try {
      assert(fileUtils.exists(tmp.getAbsolutePath))
    } finally {
      tmp.delete()
    }
  }

  test("exists should return false for non-existent path") {
    assert(!fileUtils.exists("/tmp/simplan-nonexistent-file-xyz.txt"))
  }

  test("writeContent should write string to file and readContent should return it") {
    val tmp = Files.createTempFile("simplan-test", ".txt").toFile
    try {
      val content = "hello simplan"
      fileUtils.writeContent(tmp.getAbsolutePath, content)
      assert(fileUtils.readContent(tmp.getAbsolutePath) == content)
    } finally {
      tmp.delete()
    }
  }

  test("list should return files in a directory") {
    val dir = Files.createTempDirectory("simplan-test-dir").toFile
    val child = new File(dir, "file1.txt")
    try {
      child.createNewFile()
      val listing = fileUtils.list(dir.getAbsolutePath)
      assert(listing.nonEmpty)
      assert(listing.exists(_.fileName == child.getAbsolutePath))
    } finally {
      child.delete()
      dir.delete()
    }
  }

  test("copy should copy directory contents to destination") {
    val src = Files.createTempDirectory("simplan-src").toFile
    val dst = Files.createTempDirectory("simplan-dst").toFile
    val srcFile = new File(src, "data.txt")
    try {
      srcFile.createNewFile()
      fileUtils.copy(src.getAbsolutePath, dst.getAbsolutePath)
      assert(new File(dst, "data.txt").exists())
    } finally {
      srcFile.delete()
      src.delete()
      new File(dst, "data.txt").delete()
      dst.delete()
    }
  }

  test("LocalFileUtils should be an instance of Simplan FileUtils trait") {
    assert(fileUtils.isInstanceOf[FileUtils])
  }

  test("LocalFileUtils should be an instance of Apache Commons FileUtils") {
    assert(fileUtils.isInstanceOf[org.apache.commons.io.FileUtils])
  }
}
