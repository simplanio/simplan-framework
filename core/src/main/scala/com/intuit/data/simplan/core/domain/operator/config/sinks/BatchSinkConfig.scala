package com.intuit.data.simplan.core.domain.operator.config.sinks

import com.intuit.data.simplan.core.domain.operator.OperatorConfig

/** @author Abraham, Thomas - tabraham1
  *         Created on 18-Nov-2021 at 10:50 AM
  */
class BatchSinkConfig(
    val source: String,
    val format: String,
    val location: String,
    val options: Map[String, String] = Map.empty
) extends OperatorConfig {
  def resolvedOptions: Map[String, String] = if (Option(options).isDefined) options else Map.empty[String, String]

}
