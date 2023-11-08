package com.intuit.data.simplan.logging.events

import com.intuit.data.simplan.logging.MetricConstants
import com.intuit.data.simplan.logging.domain.v2.SimplanOpsEvent

import java.time.Instant

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 12-Apr-2022 at 5:32 PM
  */
class OperatorExecutionStatusEvent(msg: String, operatorName: String, operatorType: String, taskName: String, taskIndex: Long, status: String, startTime: Option[Instant], endTime: Option[Instant] = None) extends TaskExecutionStatusEvent(msg, taskName, taskIndex, status, startTime, endTime) {
  override val action: String = MetricConstants.Action.OPERATOR_EXECUTION

  override def toOpsEvent: SimplanOpsEvent = {
    val opsEvent = super.toOpsEvent
    opsEvent.task
      .setOperatorType(operatorType)
      .setOperator(operatorName)
    opsEvent
  }
}
