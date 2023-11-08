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

package com.intuit.data.simplan.logging.domain.com.intuit.data.simplan.logging;

import com.intuit.data.simplan.logging.domain.v2.SimplanOpsEvent;
import com.intuit.data.simplan.logging.utils.JacksonJsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 03-Jan-2023 at 2:12 PM
 */
public class SimplanMetricsUtils {

    private static Logger logger = LoggerFactory.getLogger(SimplanMetricsUtils.class);

    public static SimplanOpsEvent deserialize(String simplanMetricsAsString) {
        return JacksonJsonMapper.fromJson(simplanMetricsAsString, SimplanOpsEvent.class);
    }

    public static Optional<SimplanOpsEvent> deserializeSafely(String simplanMetricsAsString) {
        try {
            return Optional.of(deserialize(simplanMetricsAsString));
        } catch (Throwable throwable) {
            logger.debug("Deserialization of Simplan ops Metrics Failed : " + throwable.getMessage());
            logger.trace("Deserialization of Simplan ops Metrics Failed : " + throwable.getMessage(), throwable);
            return Optional.empty();
        }
    }

    public static String serialize(SimplanOpsEvent simplanOpsEvent) {
        return JacksonJsonMapper.toJson(simplanOpsEvent);
    }

    public static Optional<String> serializeSafely(SimplanOpsEvent simplanOpsEvent) {
        try {
            return Optional.of(serialize(simplanOpsEvent));
        } catch (Throwable throwable) {
            logger.debug("Serialization of Simplan ops Metrics Failed : " + throwable.getMessage());
            logger.trace("Serialization of Simplan ops Metrics Failed : " + throwable.getMessage(), throwable);
            return Optional.empty();
        }
    }
}
