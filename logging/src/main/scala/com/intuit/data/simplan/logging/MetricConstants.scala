package com.intuit.data.simplan.logging

/** @author Abraham, Thomas - tabraham1
  *         Created on 16-Nov-2021 at 3:39 PM
  */
object MetricConstants extends Serializable {

  object Action {
    val APP_EXECUTION = "appExecution"
    val TASK_EXECUTION = "taskExecution"
    val OPERATOR_EXECUTION = "operatorExecution"
    val OPERATOR_DEFINITION = "operatorDefinition"
    val OPERATOR_METRICS = "operatorMetrics"
  }

  object Type {
    val PROCESS = "process"
    val AUDIT = "audit"
    val METRIC = "metric"
    val CONFIG_DEFINITION = "configDefinition"

  }

  object Level {
    val ALERT = "alert"
  }

  object Status {
    val PENDING = "pending"
    val IN_PROGRESS = "inProgress"
    val SUCCESS = "success"
    val FAILED = "failed"
  }

}
