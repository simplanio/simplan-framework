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

package com.intuit.data.simplan.core.executor

import com.intuit.data.simplan.common.config.{SimplanTasksConfiguration, TaskDefinition}
import com.intuit.data.simplan.core.context.AppContext
import com.intuit.data.simplan.core.domain.OperatorType
import com.intuit.data.simplan.core.opsmetrics.trackers.TaskExecutionTracker

import scala.util.control.Breaks.{break, breakable}
import scala.util.{Failure, Success, Try}

/** @author Abraham, Thomas - tabraham1
  *         Created on 11-Apr-2023 at 11:39 AM
  */
class SequentialDagExecutor(appContext: AppContext, dagConfig: SimplanTasksConfiguration) extends DagExecutor(appContext, dagConfig) {

  override def execute[T <: Serializable](runParameters: T): Boolean = {
    val taskOrder = dagConfig.tasks.order
    val pipelineConfStr = generatePipelineConfStr(appContext.appContextConfig)
    logger.info(s"${pipelineConfStr}")
    logger.info("Task Execution Started")
    breakable {
      taskOrder.zipWithIndex.foreach {
        case (taskName, index) => {
          val taskExecutionTracker = TaskExecutionTracker(appContext, taskName, index)
          Try {
            taskExecutionTracker.inProgress()
            val taskDefinition: TaskDefinition = dagConfig.tasks.dag(taskName)
            val triggerResponse = processTriggerAndValidationOperator(taskDefinition.trigger, OperatorType.TRIGGER, taskDefinition, taskExecutionTracker)
            if (triggerResponse) {
              val operatorResponse = processActionOperator(taskDefinition.action, taskDefinition, taskExecutionTracker)
              if (!operatorResponse.canContinue) {
                logger.info(s"Operator $taskName canContine is false. Skipping rest of the execution")
                operatorResponse.throwable match {
                  case Some(value) => logger.warn(value.getMessage, value)
                  case _           =>
                }
                break
              }
              val validationResponse = processTriggerAndValidationOperator(taskDefinition.validation, OperatorType.VALIDATION, taskDefinition, taskExecutionTracker)
              if (!validationResponse) {
                logger.info(s"Validation condition for $taskName failed. Skipping rest of the execution")
                taskExecutionTracker.failed(s"Validation condition for $taskName failed. Skipping rest of the execution")
                break
              }
            } else {
              logger.info(s"Trigger condition for $taskName failed. Skipping rest of the execution")
              taskExecutionTracker.failed(s"Trigger condition for $taskName failed. Skipping rest of the execution")
              break
            }
          } match {
            case Success(_)         => taskExecutionTracker.success()
            case Failure(exception) => taskExecutionTracker.failed(s"Task Execution failed : ${exception.getMessage}", Some(exception)); throw exception
          }
        }
      }
    }
    logger.info("Task Execution Complete")
    true
  }
}
