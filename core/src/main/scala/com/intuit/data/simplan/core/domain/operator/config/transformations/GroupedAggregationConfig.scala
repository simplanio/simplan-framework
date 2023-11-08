package com.intuit.data.simplan.core.domain.operator.config.transformations

import com.fasterxml.jackson.module.caseclass.annotation.CaseClassDeserialize
import com.intuit.data.simplan.core.domain.operator.OperatorConfig

/** @author Kiran Hiremath
  */
@CaseClassDeserialize
class GroupedAggregationConfig(
    val source: String,
    val grouping: List[String],
    val aggs: Map[String, String]
) extends OperatorConfig
