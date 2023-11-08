package com.intuit.data.simplan.core.operators

import com.intuit.data.simplan.core.context.AppContext
import com.intuit.data.simplan.core.domain.operator.{Operator, OperatorContext, OperatorRequest, OperatorResponse}
import com.intuit.data.simplan.logging.Logging

import scala.language.postfixOps

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 05-Oct-2021 at 12:29 PM
  */
class DummyOperator(appContext: AppContext, operatorContext: OperatorContext) extends Operator(appContext, operatorContext) with Logging {

  override def process(request: OperatorRequest): OperatorResponse = {
    logger.debug("Dummy operator executed")
    OperatorResponse.continue
  }
}
