package com.intuit.data.simplan.core.domain.operator.config.sources

import com.fasterxml.jackson.module.caseclass.annotation.CaseClassDeserialize
import com.intuit.data.simplan.core.domain.operator.OperatorConfig
import com.intuit.data.simplan.core.domain.{StreamingParseMode, TableType}
import com.intuit.data.simplan.global.domain.QualifiedParam

/** @author Abraham, Thomas - tabraham1
  *         Created on 17-Nov-2021 at 3:00 PM
  */
@CaseClassDeserialize
case class WaterMarkConfig(eventTime: String, delayThreshold: String)

class StreamSourceConfig(
    val format: String,
    val payloadSchema: Option[QualifiedParam],
    val headerSchema: Option[QualifiedParam] = None,
    val headerField: Option[String] = Some("key"),
    val table: Option[String] = None,
    val tableType: TableType = TableType.TEMP,
    val parseMode: Option[StreamingParseMode] = Some(StreamingParseMode.PAYLOAD_ONLY),
    val options: Map[String, String] = Map.empty,
    val watermark: Option[WaterMarkConfig] = None
) extends OperatorConfig {
  lazy val RESOLVED_PARSE_MODE: StreamingParseMode = parseMode.getOrElse(StreamingParseMode.PAYLOAD_ONLY)
  lazy val HEADER_FIELD: String = headerField.getOrElse("key")
}
