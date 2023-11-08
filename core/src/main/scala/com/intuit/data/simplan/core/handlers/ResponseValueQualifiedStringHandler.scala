package com.intuit.data.simplan.core.handlers

import com.intuit.data.simplan.common.exceptions.SimplanConfigException
import com.intuit.data.simplan.core.context.OperatorResponseManager
import com.intuit.data.simplan.global.domain.QualifiedParam
import com.intuit.data.simplan.global.exceptions.SimplanException
import com.intuit.data.simplan.global.qualifiedstring.QualifiedParamHandler

import java.io.Serializable

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 28-Mar-2022 at 6:13 PM
  */
class ResponseValueQualifiedStringHandler extends QualifiedParamHandler[Serializable] {
  private lazy val operatorResponseManager: OperatorResponseManager = OperatorResponseManager.default
  override val qualifier: String = "responseValue"

  override def resolve(qualifiedString: QualifiedParam): Serializable = {
    val strings = qualifiedString.string.split(",").map(_.trim)
    if (!operatorResponseManager.operatorResponses.contains(strings.head)) throw new SimplanConfigException(s"Response value : Not able to find task ${strings.head}")
    if (!operatorResponseManager.operatorResponses(strings.head).operatorResponse.responseValues.contains(strings.last)) throw new SimplanConfigException(s"Response value : Not able to find ResponseValue ${strings.head} in Task ${strings.head}")
    operatorResponseManager.operatorResponses(strings.head).operatorResponse.responseValues(strings.last)
  }
}
