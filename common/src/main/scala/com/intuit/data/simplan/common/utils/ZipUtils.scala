/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intuit.data.simplan.common.utils

import java.io.{File, FileInputStream, IOException}
import java.nio.file.{Files, Path, StandardCopyOption}
import java.util.zip.{ZipEntry, ZipInputStream}

/** @author Abraham, Thomas - tabraham1
  *         Created on 17-Feb-2023 at 10:54 PM
  */
object ZipUtils {

  @throws[IOException]
  def unzipFolder(source: Path, target: Path): Unit = {
    try {
      val zis = new ZipInputStream(new FileInputStream(source.toFile))
      try {
        var zipEntry = zis.getNextEntry
        while (zipEntry != null) {
          val isDirectory = if (zipEntry.getName.endsWith(File.separator)) true else false
          val newPath = zipSlipProtect(zipEntry, target)
          if (isDirectory) Files.createDirectories(newPath)
          else {
            if (newPath.getParent != null) if (Files.notExists(newPath.getParent)) Files.createDirectories(newPath.getParent)
            Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING)
          }
          zipEntry = zis.getNextEntry
        }
        zis.closeEntry()
      } finally if (zis != null) zis.close()
    }

    @throws[IOException]
    def zipSlipProtect(zipEntry: ZipEntry, targetDir: Path): Path = {
      val targetDirResolved = targetDir.resolve(zipEntry.getName)
      val normalizePath = targetDirResolved.normalize
      if (!normalizePath.startsWith(targetDir)) throw new IOException("Bad zip entry: " + zipEntry.getName)
      normalizePath
    }
  }
}
