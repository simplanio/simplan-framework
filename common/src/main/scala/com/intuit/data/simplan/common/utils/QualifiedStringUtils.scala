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

import com.intuit.data.simplan.global.domain.QualifiedParam
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}

import java.io.File
import java.net.URI

/** @author Abraham, Thomas - tabraham1
  *         Created on 26-Mar-2023 at 3:38 PM
  */
object QualifiedStringUtils {

  def readAsJsonString(qualifiedParam: QualifiedParam): String = {
    qualifiedParam.string match {
      case param if param.startsWith("classpath:") => ConfigFactory.parseResources(new URI(param).getSchemeSpecificPart).root().render(ConfigRenderOptions.concise())
      case param if param.startsWith("file:")      => ConfigFactory.parseFile(new File(new URI(param).getSchemeSpecificPart)).root().render(ConfigRenderOptions.concise())
      case param                                   => param
    }
  }
}
