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

package com.intuit.data.simplan.core.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, JsonNode}
import com.fasterxml.jackson.databind.json.JsonMapper
import com.intuit.data.simplan.core.aws.{AWSAuthType, BasicAWSAuthConfig, InstanceAWSAuthConfig}
import com.intuit.data.simplan.core.domain.operator.config.{DynamoSinkConfig, SinkConfig}

class SinkJacksonDeSerializer extends JsonDeserializer[SinkConfig] with Serializable {

  override def deserialize(p: JsonParser, ctxt: DeserializationContext):SinkConfig = {
    val objectMapper: JsonMapper = p.getCodec.asInstanceOf[JsonMapper]
    val node: JsonNode = objectMapper.readTree(p)
    val sinkType = node.get("sinkType").asText()
    sinkType match {
      case "dynamo" => {
        val tableName =  node.get("tableName").asText()
        DynamoSinkConfig(sinkType, tableName)
      }
      case _ => throw new Exception("Unknown type")
    }

  }

}
