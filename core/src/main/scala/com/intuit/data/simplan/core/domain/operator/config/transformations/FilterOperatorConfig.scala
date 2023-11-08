package com.intuit.data.simplan.core.domain.operator.config.transformations

import com.fasterxml.jackson.module.caseclass.annotation.CaseClassDeserialize

@CaseClassDeserialize
class FilterOperatorConfig(val source: String, val condition: String)
