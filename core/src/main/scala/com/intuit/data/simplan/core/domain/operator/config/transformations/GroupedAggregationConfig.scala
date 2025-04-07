package com.intuit.data.simplan.core.domain.operator.config.transformations

import com.intuit.data.simplan.core.domain.operator.OperatorConfig

/** @author Kiran Hiremath
  */
class GroupedAggregationConfig(
    val source: String,
    val grouping: List[String],
    val aggs: Map[String, String]
) extends OperatorConfig
