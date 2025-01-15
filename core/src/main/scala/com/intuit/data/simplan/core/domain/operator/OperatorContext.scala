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

package com.intuit.data.simplan.core.domain.operator

import com.intuit.data.simplan.common.config.{OperatorDefinition, SimplanTasksConfiguration, TaskDefinition}
import com.intuit.data.simplan.core.domain.OperatorType
import com.intuit.data.simplan.global.json.SimplanJsonMapper

import scala.reflect.ClassTag
import scala.util.Try

/** @author Abraham, Thomas - tabraham1
  *         Created on 09-Dec-2022 at 7:41 PM
  */
case class OperatorContext(
    taskName: String,
    taskIndex: Long,
    taskDefinition: TaskDefinition,
    operatorType: OperatorType,
    operatorDefinition: OperatorDefinition,
    appConfig: SimplanTasksConfiguration
) {
  def parseConfigAs[T](implicit m: ClassTag[T]): T = Try(SimplanJsonMapper.fromJson[T](operatorDefinition.config)) match {
    case scala.util.Success(value) => value
    case scala.util.Failure(exception) => throw new RuntimeException(s"Failed to parse config for Task $taskName(Operator : '${operatorDefinition.operator}'): ${exception.getMessage}", exception)
  }
}
