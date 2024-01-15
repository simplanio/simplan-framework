package com.intuit.data.simplan.common.logging.events

import com.intuit.data.simplan.common.config.OperatorDefinition
import com.intuit.data.simplan.global.utils.SimplanImplicits.FromJsonImplicits
import com.intuit.data.simplan.logging.MetricConstants
import com.intuit.data.simplan.logging.domain.v2.SimplanOpsEvent
import com.intuit.data.simplan.logging.domain.v2.fiedsets.ConfigDefinitionOpsEvent
import com.intuit.data.simplan.logging.events.{OperatorExecutionStatusEvent, SimplanEvent}

import scala.collection.JavaConverters._

/** @author Abraham, Thomas - tabraham1
  *         Created on 12-Apr-2022 at 5:32 PM
  */
class OperatorMetricsEvent(operatorExecutionStatusEvent: OperatorExecutionStatusEvent, operatorDefinition: OperatorDefinition, operatorType: String, taskName: String) extends SimplanEvent {

  private val _operatorDefinition: ConfigDefinitionOpsEvent = new ConfigDefinitionOpsEvent()
    .setConfig(operatorDefinition.config.fromJson[Map[String, AnyRef]].asJava)
    .setOptions(operatorDefinition.options.fromJson[Map[String, AnyRef]].asJava)

  override def toOpsEvent: SimplanOpsEvent = {
    val event = operatorExecutionStatusEvent.toOpsEvent
      .setMessage(s"Operator Metrics for $taskName($operatorType)")
      .setProcess(null)
      .setConfigDefinition(_operatorDefinition)
    event.getContext.setType(MetricConstants.Type.METRIC)
    event.getContext.setAction(MetricConstants.Action.OPERATOR_METRICS)
    event
  }
}
