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

import com.intuit.data.simplan.global.utils.SimplanImplicits.Pipe
import com.typesafe.config.{Config, ConfigRenderOptions}

/** @author Abraham, Thomas - tabraham1
 *          Created on 08-Oct-2023 at 5:54 PM
 */
object SimplanConfigRenderer {

  val renderOptions: ConfigRenderOptions = ConfigRenderOptions
    .defaults
    .setFormatted(true)
    .setJson(false)
    .setOriginComments(false)
    .setComments(true)

  def renderHocon(config: Config): String = render(config, List.empty[String])

  def renderJson(config: Config): String = config.root.render(ConfigRenderOptions.concise.setJson(true).setFormatted(true))

  def render(config: Config, includedFiles: List[String]): String = {
    val render = config.root().render(renderOptions)
      .pipe(replaceMultiLineQuotes)
      .pipe(replaceVariables)
      .replaceAllLiterally("\\n", "\n")
      .replaceAllLiterally("\\t", "\t")
    replaceIncludePlaceHolder(render, includedFiles)
  }

  private def replaceVariables(inputText: String): String = inputText.replaceAll("""\$\{variables\.""", "\\$\\{simplan.variables\\.")

  private def replaceIncludePlaceHolder(inputText: String, includes: List[String]): String = {
    val strings = includes
      .map(_.trim.replaceAll("\\s+", " "))
      .map(_.replaceAllLiterally("${CONF_HOME}/", ""))
      .map(each => s"""include "$each"""")
    inputText.replaceAllLiterally("# ##IncludePlaceholder##", strings.mkString("\n"))
  }

  private def replaceMultiLineQuotes(text: String): String = {
    val stringBuilder = new StringBuilder()
    val lines = text.split("\n")
    val pattern = """"([^"]*\\n[^"]*)"""".r

    lines.foldLeft(stringBuilder) { (acc, line) =>
      val trimmedLine = line
      val list = pattern.findAllMatchIn(trimmedLine).map(_.group(0)).toList
      val convertedString = list
        .foldLeft(trimmedLine) { (acc, m) =>
          val str = "\"\"" + m + "\"\""
          acc.replaceAllLiterally(m, str)
        }
      acc.append(convertedString).append("\n")
    }.toString()

  }
}
