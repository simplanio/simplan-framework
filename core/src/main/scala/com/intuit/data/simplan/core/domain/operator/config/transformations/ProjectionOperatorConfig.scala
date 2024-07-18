package com.intuit.data.simplan.core.domain.operator.config.transformations

import com.intuit.data.simplan.core.domain.TableType
import com.intuit.data.simplan.core.domain.operator.OperatorConfig

class ProjectionOperatorConfig(
    val source: String,
    val table: Option[String],
    val tableType: TableType = TableType.NONE,
    val projections: Seq[String]
) extends OperatorConfig
