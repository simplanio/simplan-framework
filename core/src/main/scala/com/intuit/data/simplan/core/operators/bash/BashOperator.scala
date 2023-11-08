package com.intuit.data.simplan.core.operators.bash

import com.intuit.data.simplan.core.context.AppContext
import com.intuit.data.simplan.core.domain.operator.{BaseOperator, Operator, OperatorConfig, OperatorContext, OperatorRequest, OperatorResponse}
import org.slf4j.{Logger, LoggerFactory}

import scala.language.postfixOps
import scala.sys.process._

/** @author Abraham, Thomas - tabraham1
  *         Created on 05-Oct-2021 at 12:29 PM
  */
case class BashOperatorConfig(script: String) extends OperatorConfig

class BashOperator(appContext: AppContext, operatorContext: OperatorContext) extends BaseOperator[BashOperatorConfig](appContext, operatorContext) {
  @transient lazy val logger: Logger = LoggerFactory.getLogger(classOf[BashOperator])

  override def process(request: OperatorRequest): OperatorResponse = {
    val op = (operatorConfig.script !!)
    logger.debug(op)
    OperatorResponse.continue
  }
}
