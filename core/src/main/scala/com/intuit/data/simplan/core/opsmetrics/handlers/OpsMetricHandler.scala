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
import com.intuit.data.simplan.core.opsmetrics.SimplanMetricFormatter
import com.intuit.data.simplan.logging.domain.v2.SimplanOpsEvent
import com.intuit.data.simplan.logging.domain.v2.fiedsets.EventLevel
import com.intuit.data.simplan.logging.events.SimplanEvent

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 19-Sep-2023 at 11:22 PM
  */
abstract class OpsMetricHandler(val appContext: AppContext, val opsMetricsConfig:OpsMetricsConfig) {
  def emit(metric: SimplanOpsEvent): Unit

  def trace(metric: SimplanEvent): Unit = emit(SimplanMetricFormatter.format(appContext, metric.toOpsEvent, EventLevel.TRACE))

  def debug(metric: SimplanEvent): Unit = emit(SimplanMetricFormatter.format(appContext, metric.toOpsEvent, EventLevel.DEBUG))

  def info(metric: SimplanEvent): Unit = emit(SimplanMetricFormatter.format(appContext, metric.toOpsEvent, EventLevel.INFORMATIONAL))

  def important(metric: SimplanEvent): Unit = emit(SimplanMetricFormatter.format(appContext, metric.toOpsEvent, EventLevel.IMPORTANT))

  def critical(metric: SimplanEvent): Unit = emit(SimplanMetricFormatter.format(appContext, metric.toOpsEvent, EventLevel.VERY_IMPORTANT))

  def trace(metric: SimplanEvent, throwable: Throwable): Unit = emit(SimplanMetricFormatter.format(appContext, metric.toOpsEvent, EventLevel.TRACE, Option(throwable)))

  def useful(metric: SimplanEvent, throwable: Throwable): Unit = emit(SimplanMetricFormatter.format(appContext, metric.toOpsEvent, EventLevel.DEBUG, Option(throwable)))

  def info(metric: SimplanEvent, throwable: Throwable): Unit = emit(SimplanMetricFormatter.format(appContext, metric.toOpsEvent, EventLevel.INFORMATIONAL, Option(throwable)))

  def important(metric: SimplanEvent, throwable: Throwable): Unit = emit(SimplanMetricFormatter.format(appContext, metric.toOpsEvent, EventLevel.IMPORTANT, Option(throwable)))

  def critical(metric: SimplanEvent, throwable: Throwable): Unit = emit(SimplanMetricFormatter.format(appContext, metric.toOpsEvent, EventLevel.VERY_IMPORTANT, Option(throwable)))
}
