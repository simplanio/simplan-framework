package com.intuit.data.simplan.core.domain.operator

import com.intuit.data.simplan.common.config.OperatorDefinition
import com.intuit.data.simplan.core.context.AppContext

/** @author - Abraham, Thomas - tabaraham1
  *         Created on 8/19/21 at 10:57 PM
  */
abstract class Operator(appContext: AppContext, operatorContext: OperatorContext) extends Serializable {
  def process(request: OperatorRequest): OperatorResponse
  def validateDefinition(definition: OperatorDefinition): Boolean = true
}
