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

package com.intuit.data.simplan.common.config.parser

import com.intuit.data.simplan.common.exceptions.SimplanConfigException
import com.intuit.data.simplan.common.files.FileUtils
import com.intuit.data.simplan.logging.Logging
import com.typesafe.config._
import com.typesafe.config.impl.Parseable
import org.apache.commons.lang.StringUtils

import java.io.File
import java.net.URI

/** @author Abraham, Thomas - tabraham1
  *         Created on 08-Dec-2022 at 1:35 PM
  */
class TypesafeObjectStorageIncluder extends ConfigIncluder with ConfigIncluderClasspath with ConfigIncluderFile with Logging {
  private var fallback = Parseable.newString(StringUtils.EMPTY, ConfigParseOptions.defaults.setAllowMissing(false)).options.getIncluder
  private val systemProperties: Config = ConfigFactory.systemProperties()

  override def withFallback(fallback: ConfigIncluder): ConfigIncluder = {
    this.fallback = fallback; this
  }

  override def include(context: ConfigIncludeContext, includeString: String): ConfigObject = {
    val resolvedInclude = replaceMatchesWithEnvVariable(includeString)
    val uri = new URI(resolvedInclude)

    uri.getScheme match {
      case null | "file" => includeFile(context, new File(uri.getPath))
      case "classpath"   => includeResources(context, uri.getPath)
      case "s3" | "s3a" =>
        val utils = Class.forName("com.intuit.data.simplan.core.aws.DefaultAmazonS3FileUtils").newInstance().asInstanceOf[FileUtils]
        val str = utils.readContent(resolvedInclude)
        val configObject = ConfigFactory.parseString(str, context.parseOptions()).root()
        configObject.withFallback(fallback.include(context, resolvedInclude))
      case _ => fallback.include(context, includeString)
    }
  }

  override def includeResources(context: ConfigIncludeContext, resource: String): ConfigObject = {
    val resolvedResource = replaceMatchesWithEnvVariable(resource)
    // ConfigFactory.parseResourcesAnySyntax(resolvedResource, context.parseOptions).root
    fallback.asInstanceOf[ConfigIncluderClasspath].includeResources(context, resolvedResource)

  }

  override def includeFile(context: ConfigIncludeContext, file: File): ConfigObject = {
    val resolvedResource = replaceMatchesWithEnvVariable(file.getPath)
    val resolvedFile = new File(resolvedResource)
    fallback.asInstanceOf[ConfigIncluderFile].includeFile(context, resolvedFile)

  }

  def replaceMatchesWithEnvVariable(path: String): String = {
    val regex = "\\$\\{(\\w+)\\}".r
    val resolvedPath = regex.replaceAllIn(
      path,
      m => {
        val key = m.group(1)
        TypesafeConfigUtils.getString(systemProperties, key) match {
          case Some(value) => value
          case None        => throw new SimplanConfigException(s"ConfigInclude: Environment value '$key' not found for '$path' used in include statement")
        }
      }
    )
    logger.info(s"Attempting to include Config File: $resolvedPath")
    resolvedPath
  }
}
