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

import com.intuit.data.simplan.common.config.{OperatorDefinition, SimplanAppContextConfiguration, SimplanTasksConfiguration, TaskDefinition}
import com.intuit.data.simplan.common.exceptions.SimplanConfigException
import com.intuit.data.simplan.common.logging.events.OperatorDefinitionEvent
import com.intuit.data.simplan.common.utils.InitUtils
import com.intuit.data.simplan.core.context.{AppContext, OperatorResponseManager, Support}
import com.intuit.data.simplan.core.domain._
import com.intuit.data.simplan.core.domain.operator._
import com.intuit.data.simplan.core.opsmetrics.trackers.{OperationExecutionTracker, TaskExecutionTracker}
import com.intuit.data.simplan.logging.events.OperatorExecutionStatusEvent
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}

/** @author Abraham, Thomas - tabraham1
 *          Created on 11-Apr-2023 at 11:13 AM
 */
abstract class DagExecutor(appContext: AppContext, dagConfig: SimplanTasksConfiguration) extends Serializable {
  lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val operatorResponseManager: OperatorResponseManager = OperatorResponseManager.default

  def execute[T <: Serializable](runParameters: T): Boolean

  def simulate[T <: Serializable](runParameters: T, taskName: String): Map[String, OperatorResponse]

  def processActionOperator(actionOperatorConfig: OperatorDefinition, taskConfig: TaskDefinition, taskExecutionTracker: TaskExecutionTracker): OperatorResponse = {

    val operatorExecution = OperationExecutionTracker(appContext, actionOperatorConfig.operator, OperatorType.ACTION.toString, taskExecutionTracker)
    val taskName = taskExecutionTracker.taskName
    Try {
      implicit val xcoms: Map[String, XComWrapper] = operatorResponseManager.operatorResponses
      logger.info(s"Start Executing $taskName(action)")
      val inProgressStatusEvent = operatorExecution.inProgress()
      emitOperatorDefinition(OperatorType.ACTION, taskExecutionTracker, actionOperatorConfig, inProgressStatusEvent)
      val operator: Operator = instantiateOperator(appContext, taskExecutionTracker, taskConfig, OperatorType.ACTION, actionOperatorConfig)
      val operatorRequest = new OperatorRequest(operatorResponseManager.operatorResponses)
      val response = operator.process(operatorRequest)
      val wrapper = XComWrapper(operatorClass = operator.getClass, operatorResponse = response)
      operatorResponseManager.append(taskName -> wrapper)
      response
    } match {
      case Success(value) =>
        logger.info(s"Completed Executing $taskName(action) - Result ${value.canContinue}")
        operatorExecution.success()
        value
      case Failure(exception) => operatorExecution.failed(s"Operator Execution failed : ${exception.getMessage}", exception); throw exception
    }
  }

  def processTriggerAndValidationOperator(
                                           operatorConfigOption: Option[OperatorDefinition],
                                           operatorType: OperatorType,
                                           taskConfig: TaskDefinition,
                                           taskExecutionTracker: TaskExecutionTracker): Boolean = {
    operatorConfigOption match {
      case Some(operatorConfig) =>
        val operatorExecution = OperationExecutionTracker(appContext, operatorConfig.operator, operatorType.toString, taskExecutionTracker)

        Try {
          logger.info(s"Starting task: ${taskExecutionTracker.taskName}($operatorType) - Operator: ${operatorConfig.operator}")
          val inProgressEvent = operatorExecution.inProgress()
          emitOperatorDefinition(operatorType, taskExecutionTracker, operatorConfig, inProgressEvent)
          val operator: Operator = instantiateOperator(appContext, taskExecutionTracker, taskConfig, operatorType, operatorConfig)
          val operatorRequest: OperatorRequest = new OperatorRequest(operatorResponseManager.operatorResponses)
          val response = operator.process(operatorRequest)
          response.canContinue
        } match {
          case Success(canContinue) =>
            logger.info(s"Completed Executing ${taskExecutionTracker.taskName}($operatorType) - Result ${canContinue}")
            operatorExecution.success();
            canContinue
          case Failure(exception) => operatorExecution.failed(s"Operation Execution Failed : ${exception.getMessage}", exception); throw exception
        }
      case None => {
        logger.info(s"Skipped: No $operatorType defined for ${taskExecutionTracker.taskName} - Retuning true")
        true
      }
    }
  }

  private def emitOperatorDefinition(
                                      operatorType: OperatorType,
                                      taskExecutionTracker: TaskExecutionTracker,
                                      operatorConfig: OperatorDefinition,
                                      operatorExecutionStatusEvent: OperatorExecutionStatusEvent): Unit = {
    Try(appContext.opsMetricsEmitter.info(new OperatorDefinitionEvent(operatorExecutionStatusEvent, operatorConfig, operatorType.toString, taskExecutionTracker.taskName))) match {
      case Success(_) => logger.info(s"Successfully emitted OperatorDefinitionEvent for Task ${taskExecutionTracker.taskName}($operatorType)")
      case Failure(exception) => logger.warn(s"Failed to emit OperatorDefinitionEvent for Task ${taskExecutionTracker.taskName}($operatorType)", exception)
    }
  }

  def instantiateOperator(appContext: Support, taskExecutionTracker: TaskExecutionTracker, taskDefinition: TaskDefinition, operatorType: OperatorType, operatorDefinition: OperatorDefinition): Operator = {
    try {
      val className = appContext.appContextConfig.application.operatorMappings.getOrElse(operatorDefinition.operator, operatorDefinition.operator)
      logger.info(s"Instantiating Operator: $className")
      val cls = Class.forName(className)
      try {
        InitUtils.instantiate[Operator](
          cls,
          List(
            appContext,
            OperatorContext(taskExecutionTracker.taskName, taskExecutionTracker.taskIndex, taskDefinition, operatorType, operatorDefinition, dagConfig)
          )
        )
      } catch {
        case illegalArgumentException: Exception =>
          throw new SimplanConfigException(s"Unable to Instantiate ${cls.getCanonicalName}. Please ensure application is started with ${appContext.getClass}", illegalArgumentException)
      }
    } catch {
      case classNotFoundException: ClassNotFoundException =>
        throw new SimplanConfigException(
          s"Unable to find class ${appContext.appContextConfig.application.operatorMappings.getOrElse(operatorDefinition.operator, operatorDefinition.operator)}. Please ensure that the mapping is correct and the class is available in classpath.",
          classNotFoundException)
    }
  }

  def generatePipelineConfStr(config: SimplanAppContextConfiguration): String = {
    val stepNames: String = dagConfig.tasks.order.mkString(",\n")
    val stepsInfoStr: String =
      s"""
         |steps = [
         |${stepNames}
         |]
       """.stripMargin

    val stepDetails: Array[String] = dagConfig.tasks.dag.map(task => {
      s"""
         |${task._1} = {
         |sql = \"\"\"
         |    ${task._2.action.config}
         |   \"\"\"
         |}
      """.stripMargin
    }).toArray

    val stepDetailsStr: String = stepDetails.mkString(",\n")

    val pipelineConfig: String =
      s"""
         |pipeline {
         |name = ${config.application.name}${stepsInfoStr}
         |}
         |steps {
         |${stepDetailsStr}
         |}
        """.stripMargin

    pipelineConfig
  }
}
