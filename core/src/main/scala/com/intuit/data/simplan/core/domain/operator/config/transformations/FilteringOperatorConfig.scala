package com.intuit.data.simplan.core.domain.operator.config.transformations

import com.fasterxml.jackson.module.caseclass.annotation.CaseClassDeserialize
import com.intuit.data.simplan.core.domain.operator.OperatorConfig

/** @author Abraham, Thomas - tabraham1
  *         Created on 18-Nov-2021 at 5:45 PM
  */
@CaseClassDeserialize
class FilteringOperatorConfig(val source: String, val condition: String) extends OperatorConfig
