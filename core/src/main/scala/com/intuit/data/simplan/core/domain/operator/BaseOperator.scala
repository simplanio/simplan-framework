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

import com.intuit.data.simplan.core.context.AppContext

import scala.reflect.ClassTag

/** @author Abraham, Thomas - tabraham1
  *         Created on 09-Dec-2022 at 5:26 PM
  */
abstract class BaseOperator[T <: OperatorConfig](
    applicationContext: AppContext,
    operatorContext: OperatorContext)(implicit m: ClassTag[T])
    extends Operator(applicationContext, operatorContext) {
  implicit lazy val operatorConfig: T = operatorContext.parseConfigAs[T]
}
