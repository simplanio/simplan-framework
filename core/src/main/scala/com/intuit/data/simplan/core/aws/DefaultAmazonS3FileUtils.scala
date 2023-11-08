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

package com.intuit.data.simplan.core.aws

import com.intuit.data.simplan.common.files.{FileListing, FileUtils}

/** @author Abraham, Thomas - tabraham1
  *         Created on 08-Dec-2022 at 4:01 PM
  */
class DefaultAmazonS3FileUtils extends FileUtils {
  private val amazonS3FileUtils = AmazonS3FileUtils(None)

  override def readContent(path: String, charset: String): String = amazonS3FileUtils.readContent(path, charset)

  override def exists(path: String): Boolean = amazonS3FileUtils.exists(path)

  override def list(path: String, recursive: Boolean = true, filter: FileListing => Boolean = _ => true): List[FileListing] = amazonS3FileUtils.list(path, recursive, filter)

  override def copy(sourcePath: String, destinationPath: String): Boolean = ???
  override def writeContent(path: String, content: String): Boolean = amazonS3FileUtils.writeContent(path, content)

  override def getCountAndSize(s3Path: String): (Long, Long) = amazonS3FileUtils.getCountAndSize(s3Path)
}
