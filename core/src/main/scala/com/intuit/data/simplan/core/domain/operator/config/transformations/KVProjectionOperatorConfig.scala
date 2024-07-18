package com.intuit.data.simplan.core.domain.operator.config.transformations

import com.intuit.data.simplan.core.domain.TableType
import com.intuit.data.simplan.core.domain.operator.OperatorConfig

class KVProjectionOperatorConfig(
    val source: String,
    val table: Option[String],
    val tableType: TableType = TableType.NONE,
    val projections: Map[String,String]
) extends OperatorConfig
