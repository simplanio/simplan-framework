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

import com.fasterxml.jackson.annotation.{JsonAnyGetter, JsonAnySetter, JsonIgnore}

import java.util
import scala.collection.JavaConverters.mapAsScalaMapConverter

/** @author Abraham, Thomas - tabraham1
  *         Created on 09-Dec-2022 at 4:53 PM
  */
trait OperatorConfig extends Serializable {

  lazy val additionalProperties: Map[String, AnyRef] = _additionalProperties.asScala.toMap

  @JsonIgnore private val _additionalProperties: util.HashMap[String, AnyRef] = new util.HashMap[String, AnyRef]
  @JsonAnyGetter def getAdditionalProperties: util.Map[String, AnyRef] = this._additionalProperties

  @JsonAnySetter def setAdditionalProperty(name: String, value: AnyRef): OperatorConfig = {
    this._additionalProperties.put(name, value)
    this
  }
}