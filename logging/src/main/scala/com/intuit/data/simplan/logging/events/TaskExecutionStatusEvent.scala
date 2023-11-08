package com.intuit.data.simplan.logging.events

import com.intuit.data.simplan.logging.MetricConstants
import com.intuit.data.simplan.logging.domain.v2.SimplanOpsEvent
import com.intuit.data.simplan.logging.domain.v2.fiedsets.TaskOpsEvent

import java.time.Instant

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 12-Apr-2022 at 5:17 PM
  */
class TaskExecutionStatusEvent(msg: String, taskName: String, taskIndex: Long, status: String, startTime: Option[Instant], endTime: Option[Instant] = None) extends ApplicationExecutionStatusEvent(msg, taskName, status, startTime, endTime) {
  override val action: String = MetricConstants.Action.TASK_EXECUTION

  val task: TaskOpsEvent = new TaskOpsEvent()
    .setName(taskName)
    .setIndex(taskIndex)

  override def toOpsEvent: SimplanOpsEvent = super.toOpsEvent.setTask(task)
}
