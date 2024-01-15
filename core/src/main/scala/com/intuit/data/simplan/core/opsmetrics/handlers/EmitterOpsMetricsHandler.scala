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
import com.intuit.data.simplan.logging.Logging
import com.intuit.data.simplan.logging.domain.v2.SimplanOpsEvent
import com.intuit.data.simplan.logging.domain.v2.fiedsets.EventLevel

/** @author Abraham, Thomas - tabraham1
  *         Created on 19-Sep-2023 at 11:58 PM
  */
class EmitterOpsMetricsHandler(appContext: AppContext, opsMetricsConfig: OpsMetricsConfig) extends OpsMetricHandler(appContext, opsMetricsConfig) with Logging {

  private val emitterName = opsMetricsConfig.resolvedConfig.getOrElse("emitterName", "opsMetrics")
  private val level = opsMetricsConfig.resolvedConfig.getOrElse("level", EventLevel.INFORMATIONAL.toString)
  private val levelEnum = EventLevel.valueOf(level)
  private lazy val emitter = appContext.emitters.get(emitterName)

  override protected def emit(metric: SimplanOpsEvent): Unit = {
    if (metric.context.getLevel.ordinal() >= levelEnum.ordinal()) {
      emitter.emitObjectWithKey(metric, metric.context.getRunId)
    }
  }

}
