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

package com.intuit.data.simplan.core.opsmetrics.handlers

import com.intuit.data.simplan.common.config.OpsMetricsConfig
import com.intuit.data.simplan.core.context.AppContext
import com.intuit.data.simplan.logging.domain.v2.SimplanOpsEvent
import com.intuit.data.simplan.logging.domain.v2.fiedsets.EventLevel
import com.intuit.data.simplan.logging.utils.JacksonJsonMapper
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 19-Sep-2023 at 11:22 PM
  */
class Slf4JOpsMetricHandler(appContext: AppContext, opsMetricsConfig: OpsMetricsConfig) extends OpsMetricHandler(appContext, opsMetricsConfig) {

  private val loggerName = opsMetricsConfig.resolvedConfig.getOrElse("loggerName", "simplanMetrics")

  @transient private lazy val logger: Logger = LoggerFactory.getLogger(loggerName)

  override def emit(metric: SimplanOpsEvent): Unit = {
    val string = JacksonJsonMapper.toJson(metric)
    metric.context.getLevel match {
      case EventLevel.TRACE          => logger.trace(string)
      case EventLevel.DEBUG          => logger.debug(string)
      case EventLevel.INFORMATIONAL  => logger.info(string)
      case EventLevel.IMPORTANT      => logger.warn(string)
      case EventLevel.VERY_IMPORTANT => logger.error(string)
    }
  }
}
