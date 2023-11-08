package com.intuit.data.simplan.core.domain.operator.config.transformations

import com.intuit.data.simplan.core.domain.operator.OperatorConfig

trait WindowOperatorConfig extends Serializable {
  def windowType: String
  def timeColumn: String
  def windowDuration: String
}
case class TumbleWindowOperatorConfig(windowType: String, timeColumn: String, windowDuration: String) extends WindowOperatorConfig
case class SlidingWindowOperatorConfig(windowType: String, timeColumn: String, windowDuration: String, slideDuration: String) extends WindowOperatorConfig
case class SessionWindowOperatorConfig(windowType: String, timeColumn: String, windowDuration: String, slideDuration: String, startTime: String) extends WindowOperatorConfig
class WindowConfig(val source: String, val window: WindowOperatorConfig) extends OperatorConfig