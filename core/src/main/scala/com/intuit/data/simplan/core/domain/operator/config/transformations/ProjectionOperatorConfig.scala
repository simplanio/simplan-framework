package com.intuit.data.simplan.core.domain.operator.config.transformations

import com.fasterxml.jackson.module.caseclass.annotation.CaseClassDeserialize
import com.intuit.data.simplan.core.domain.TableType
import com.intuit.data.simplan.core.domain.operator.OperatorConfig

@CaseClassDeserialize
class ProjectionOperatorConfig(
    val source: String,
    val table: Option[String],
    val tableType: TableType = TableType.NONE,
    val projections: Seq[String]
) extends OperatorConfig
