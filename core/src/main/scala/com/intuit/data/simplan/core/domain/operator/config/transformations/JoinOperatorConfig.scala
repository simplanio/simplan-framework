package com.intuit.data.simplan.core.domain.operator.config.transformations

import com.fasterxml.jackson.module.caseclass.annotation.CaseClassDeserialize
import com.intuit.data.simplan.core.domain.operator.OperatorConfig

/** @author Abraham, Thomas - tabraham1
  *         Created on 17-Nov-2021 at 3:02 PM
  */
@CaseClassDeserialize
class JoinOperatorConfig(
    val leftTable: String,
    val rightTable: String,
    val joinCondition: String,
    val joinType: String
) extends OperatorConfig
