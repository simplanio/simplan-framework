package com.intuit.data.simplan.core.domain.operator.config.transformations

import com.intuit.data.simplan.core.domain.TableType
import com.intuit.data.simplan.core.domain.operator.OperatorConfig

case class KVProjectionOperatorConfig(
    source: String,
    table: Option[String],
    tableType: TableType = TableType.NONE,
    projections: Map[String,String]
) extends OperatorConfig
