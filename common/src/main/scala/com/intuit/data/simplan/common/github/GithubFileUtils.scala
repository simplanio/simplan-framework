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

package com.intuit.data.simplan.common.github

import com.intuit.data.simplan.common.files.{FileListing, FileUtils}

/** @author Abraham, Thomas - tabraham1
  *         Created on 19-Jul-2023 at 11:39 AM
  */
class GithubFileUtils(host: String, token: String, scheme: String = "https", port: Int = -1, defaultBranch: String = "master") extends FileUtils {
  override val schemes: List[String] = List("github")

  private val handler = new GithubHandler(host, token, scheme, port, defaultBranch)
  override def readContent(path: String, charset: String): String = handler.getFileContents(path)

  override def exists(path: String): Boolean = ???

  override def copy(sourcePath: String, destinationPath: String): Boolean = ???

  override def list(path: String, recursive: Boolean, filter: FileListing => Boolean): List[FileListing] = ???

  override def writeContent(path: String, content: String): Boolean = ???

  override def getCountAndSize(path: String): (Long, Long) = ???
}
