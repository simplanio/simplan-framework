package com.intuit.data.simplan.core.domain.operator.config.sinks

import com.fasterxml.jackson.module.caseclass.annotation.CaseClassDeserialize
import com.intuit.data.simplan.core.domain.operator.OperatorConfig

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 17-Nov-2021 at 3:02 PM
  */
@CaseClassDeserialize
class StreamingSinkConfig(
    val source: String,
    val outputMode: String,
    val format: String,
    val options: Map[String, String] = Map.empty,
    val awaitTermination: Boolean = true,
) extends OperatorConfig

