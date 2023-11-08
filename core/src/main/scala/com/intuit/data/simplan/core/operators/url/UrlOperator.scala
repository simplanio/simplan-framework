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

package com.intuit.data.simplan.core.operators.url

import com.intuit.data.simplan.core.context.AppContext
import com.intuit.data.simplan.core.domain.operator._
import com.intuit.data.simplan.global.utils.ExecutionUtils.retry
import org.apache.commons.lang3.StringUtils
import org.slf4j.{Logger, LoggerFactory}

case class UrlOperatorConfig(url: String, params: Array[String]) extends OperatorConfig

abstract class UrlOperator(appContext: AppContext, operatorContext: OperatorContext) extends BaseOperator[UrlOperatorConfig](appContext, operatorContext) {
  @transient lazy val logger: Logger = LoggerFactory.getLogger(classOf[UrlOperator])
  val RESPONSE_VALUE_KEY = "urlContent"

  override def process(request: OperatorRequest): OperatorResponse = {
    val urlContent: String = retry(5, 5) {
      getUrlContent(operatorConfig)
    }

    if (StringUtils.isEmpty(urlContent)) {
      OperatorResponse.dontContinue
    } else {
      new OperatorResponse(true, Map(RESPONSE_VALUE_KEY -> urlContent))
    }
  }

  def getUrlContent(config: UrlOperatorConfig): String = StringUtils.EMPTY

}
