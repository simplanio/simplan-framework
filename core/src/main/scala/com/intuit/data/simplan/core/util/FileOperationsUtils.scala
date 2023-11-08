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

package com.intuit.data.simplan.core.util

import com.intuit.data.simplan.common.files.{FileListing, FileUtils}
import com.intuit.data.simplan.global.exceptions.SimplanException
import com.intuit.data.simplan.parser.utils.RegexUtils.RichRegex
import org.apache.commons.io.FilenameUtils

/** @author Abraham, Thomas - tabraham1
  *         Created on 05-Jan-2023 at 2:48 PM
  */
object FileOperationsUtils {

  def getSortedDirectory(fileUtils: FileUtils, path: String, directorySortPath: Option[String]): String = {
    //Return path if directoryPatternSorted is not defined
    if (directorySortPath.isEmpty) return path
    val directoryPattern = (path.replaceAll("/", "\\/") + directorySortPath.get+ "\\/.*").r
    // Else return sorted path
    val filter = (each: FileListing) => (directoryPattern matches each.fileName) && each.fileName.endsWith("/_SUCCESS")
    val listings = fileUtils.list(path, filter = filter)
    listings match {
      case listing if listing.nonEmpty => FilenameUtils.getPath(listing.maxBy(_.lastModifiedDate).fileName)
      case _                           => throw new SimplanException(s"No directory found for pattern $directoryPattern")
    }
  }
}
