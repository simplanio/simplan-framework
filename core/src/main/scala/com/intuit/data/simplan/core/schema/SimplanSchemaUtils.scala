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

package com.intuit.data.simplan.core.schema

import com.intuit.data.simplan.global.utils.SimplanImplicits._

/** @author Abraham, Thomas - tabraham1
  *         Created on 05-Oct-2022 at 5:20 PM
  */
object SimplanSchemaUtils {

  def generateSchemaFields(schema: StructType): String = {
    schema.fields.map(field => s"${field.name} ${field.`type`}").mkString(", ")
  }

  def fromJson(json: String): StructType = json.fromJson[StructType]
}
