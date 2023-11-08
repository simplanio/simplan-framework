package com.intuit.data.simplan.core.opsmetrics.trackers

import com.intuit.data.simplan.core.context.AppContext
import com.intuit.data.simplan.logging.MetricConstants
import com.intuit.data.simplan.logging.events.ApplicationExecutionStatusEvent

import java.time.Instant

/** @author Abraham, Thomas - tabraham1
  *         Created on 17-Feb-2022 at 6:32 PM
  */
case class ApplicationExecutionTracker(appContext: AppContext, appName: String, startTime: Option[Instant] = Some(Instant.now)) extends Serializable {
  private val emitter =appContext.opsMetricsEmitter
  emitter.important(new ApplicationExecutionStatusEvent("Application execution in progress", appName, MetricConstants.Status.IN_PROGRESS, Option(Instant.now), None))

  def success(message: String = "Application completed successfully"): Unit =
    emitter.important(new ApplicationExecutionStatusEvent(message, appName, MetricConstants.Status.SUCCESS, startTime, Option(Instant.now)))

  def failed(message: String = "Application execution failed", exception: Option[Throwable]): Unit = {
    if (exception.isEmpty)
      emitter.important(new ApplicationExecutionStatusEvent(message, appName, MetricConstants.Status.FAILED, startTime, Option(Instant.now)))
    else emitter.important(new ApplicationExecutionStatusEvent(message, appName, MetricConstants.Status.FAILED, startTime, Option(Instant.now)), exception.get)

  }
}
