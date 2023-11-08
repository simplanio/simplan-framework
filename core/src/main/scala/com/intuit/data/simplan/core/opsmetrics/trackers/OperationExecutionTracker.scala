package com.intuit.data.simplan.core.opsmetrics.trackers

import com.intuit.data.simplan.core.context.AppContext
import com.intuit.data.simplan.logging.MetricConstants
import com.intuit.data.simplan.logging.events.OperatorExecutionStatusEvent

import java.time.Instant

/** @author Abraham, Thomas - tabraham1
  *         Created on 17-Feb-2022 at 6:32 PM
  */
case class OperationExecutionTracker(appContext: AppContext, operatorName: String, operatorType: String, taskExecutionTracker: TaskExecutionTracker) extends Serializable {
  var startTime: Option[Instant] = None
  private val emitter = appContext.opsMetricsEmitter

  def inProgress(message: String = "Operation execution in progress"): OperatorExecutionStatusEvent = {
    startTime = Some(Instant.now)
    val event = new OperatorExecutionStatusEvent(
      message,
      operatorName,
      operatorType,
      taskExecutionTracker.taskName,
      taskExecutionTracker.taskIndex,
      MetricConstants.Status.IN_PROGRESS,
      Some(Instant.now),
      None
    )
    emitter.important(event)
    event
  }

  def success(message: String = "Operation completed successfully"): Unit = {
    val event = new OperatorExecutionStatusEvent(
      message,
      operatorName,
      operatorType,
      taskExecutionTracker.taskName,
      taskExecutionTracker.taskIndex,
      MetricConstants.Status.SUCCESS,
      startTime,
      Option(Instant.now)
    )
    emitter.important(event)
    event
  }

  def failed(message: String): OperatorExecutionStatusEvent = {
    val event = new OperatorExecutionStatusEvent(
      message,
      operatorName,
      operatorType,
      taskExecutionTracker.taskName,
      taskExecutionTracker.taskIndex,
      MetricConstants.Status.FAILED,
      startTime,
      Option(Instant.now)
    )
    emitter.important(event)
    event
  }

  def failed(message: String, exception: Throwable): OperatorExecutionStatusEvent = {
    val event = new OperatorExecutionStatusEvent(
      message,
      operatorName,
      operatorType,
      taskExecutionTracker.taskName,
      taskExecutionTracker.taskIndex,
      MetricConstants.Status.FAILED,
      startTime,
      Option(Instant.now)
    )
    emitter.important(event, exception)
    event
  }
}
