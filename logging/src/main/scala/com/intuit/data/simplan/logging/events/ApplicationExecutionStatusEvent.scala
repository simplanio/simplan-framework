package com.intuit.data.simplan.logging.events

import com.intuit.data.simplan.logging.MetricConstants
import com.intuit.data.simplan.logging.domain.v2.SimplanOpsEvent
import com.intuit.data.simplan.logging.domain.v2.fiedsets.{ContextOpsEvent, ProcessOpsEvent}

import java.time.Instant

/** @author Abraham, Thomas - tabraham1
  *         Created on 12-Apr-2022 at 4:11 PM
  */
class ApplicationExecutionStatusEvent(msg: String, appName: String, val status: String, val startTime: Option[Instant], val endTime: Option[Instant] = None, additionalContextAttributes: Map[String, AnyRef] = Map.empty) extends SimplanEvent {
  override val message: String = msg
  val action: String = MetricConstants.Action.APP_EXECUTION

  private lazy val context = new ContextOpsEvent()
    .setAction(action)
    .setSubject(appName)
    .setType(MetricConstants.Type.PROCESS)
  additionalContextAttributes.foreach { case (k, v) => context.setAdditionalProperty(k, v) }

  private lazy val process = new ProcessOpsEvent()
    .setStatus(status)
    .setStart(startTime.orNull)
    .setEnd(endTime.orNull)
    .setName(appName)

  override def toOpsEvent: SimplanOpsEvent = super.toOpsEvent.setContext(context).setProcess(process)
}
