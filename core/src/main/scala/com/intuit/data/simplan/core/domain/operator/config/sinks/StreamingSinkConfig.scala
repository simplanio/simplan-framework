package com.intuit.data.simplan.core.domain.operator.config.sinks

import com.intuit.data.simplan.core.domain.SinkLocationType
import com.intuit.data.simplan.core.domain.operator.OperatorConfig

/**
 * @author Abraham, Thomas - tabraham1
 *         Created on 17-Nov-2021 at 3:02 PM
 */
class StreamingSinkConfig(
                           val source: String,
                           val outputMode: String,
                           val format: String,
                           val location: String,
                           val options: Map[String, String] = Map.empty,
                           val awaitTermination: Boolean = true,
                           val locationType: Option[SinkLocationType] = None
                         ) extends OperatorConfig

