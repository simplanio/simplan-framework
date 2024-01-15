package com.intuit.data.simplan.core.opsmetrics.trackers

import com.intuit.data.simplan.core.context.AppContext
import com.intuit.data.simplan.logging.MetricConstants
import com.intuit.data.simplan.logging.events.TaskExecutionStatusEvent
import org.apache.commons.lang.time.DurationFormatUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.TimeZone
import scala.collection.mutable

/** @author Abraham, Thomas - tabraham1
  *         Created on 17-Feb-2022 at 6:32 PM
  */
case class TaskExecutionTracker(appContext: AppContext, taskName: String, taskIndex: Long) extends Serializable {
  private var startTime: Option[Instant] = None
  @transient lazy private val logger = LoggerFactory.getLogger(classOf[TaskExecutionTracker])
  private val emitter = appContext.opsMetricsEmitter

  val stepInfoFields: mutable.LinkedHashMap[String, String] = mutable.LinkedHashMap.empty[String, String]
  val dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
  dateFormatter.setTimeZone(TimeZone.getTimeZone("Etc/UTC"))

  def pending(message: String = "Task execution in pending"): Unit = {
    emitter.important(new TaskExecutionStatusEvent(message, taskName, taskIndex, MetricConstants.Status.PENDING, Some(Instant.now), None))
  }

  def inProgress(message: String = "Task execution in progress"): Unit = {
    startTime = Some(Instant.now)
    //Logging step level info, which will be leveraged by Superlue LogHelpErr
    stepInfoFields += ("step" -> taskName)
    stepInfoFields += ("startTime" -> dateFormatter.format(startTime.get.toEpochMilli))
    logger.info("#StepInfo: stage=started; " + stepInfoFields.map(each => each._1 + "=" + each._2).mkString("; "))

    emitter.important(new TaskExecutionStatusEvent(message, taskName, taskIndex, MetricConstants.Status.IN_PROGRESS, Some(Instant.now), None))
  }

  def success(message: String = "Task completed successfully"): Unit = {
    stepInfoFields += ("status" -> "success")
    stepInfoFields += ("canContinue" -> "true")
    stepInfoFields += ("errorCount" -> "0")

    emitter.important(new TaskExecutionStatusEvent(message, taskName, taskIndex, MetricConstants.Status.SUCCESS, startTime, Some(Instant.now)))
    instrumentJobCompletion()
  }

  def failed(message: String = "Task execution failed", exception: Option[Throwable] = None): Unit = {
    val operationName =
      try {
        // format is STEP-operationname_
        StringUtils.substringBetween(taskName, "STEP-", "_")
      } catch {
        // parsing error
        case ex: Exception => "UNKNOWN"
      }
    val operation = s"${taskName}-${operationName}"

    if (exception.isEmpty)
      emitter.important(new TaskExecutionStatusEvent(message, taskName, taskIndex, MetricConstants.Status.FAILED, startTime, Some(Instant.now)))
    else {
      val errors = s"{ operation=${operation}; exception=${exception.get.getMessage}}"
      stepInfoFields += ("errors" -> s"[$errors]")
      emitter.important(new TaskExecutionStatusEvent(message, taskName, taskIndex, MetricConstants.Status.FAILED, startTime, Some(Instant.now)), exception.get)
    }
    stepInfoFields += ("canContinue" -> "false")
    stepInfoFields += ("status" -> "failed")
    stepInfoFields += ("errorCount" -> "1")
    stepInfoFields += ("errorCount" -> "1")
    instrumentJobCompletion()
  }

  def instrumentJobCompletion() = {
    val endTime = Instant.now().toEpochMilli
    stepInfoFields += ("duration" -> DurationFormatUtils.formatDurationWords(endTime - startTime.get.toEpochMilli, true, true))
    stepInfoFields += ("endTime" -> dateFormatter.format(endTime))
    logger.info("#StepInfo: stage=completed; " + stepInfoFields.map(each => each._1 + "=" + each._2).mkString("; "))
  }
}
