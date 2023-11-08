package com.intuit.data.simplan.logging.layout

import com.intuit.data.simplan.logging.MdcConstants
import com.intuit.data.simplan.logging.domain.v2.SimplanOpsEvent
import com.intuit.data.simplan.logging.domain.v2.fiedsets.{ContextOpsEvent, ErrorOpsEvent, EventLevel, MetaOpsEvent}
import com.intuit.data.simplan.logging.utils.JacksonJsonMapper
import org.apache.commons.lang.StringUtils
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.{Layout, Level}

import java.time.Instant
import scala.collection.JavaConverters._
import scala.util.Try

/** @author Abraham, Thomas - tabraham1
  *         Created on 12-Nov-2021 at 9:13 AM
  */
class SimplanMetricLayout(locationInfo: Boolean) extends Layout {

  def this() = this(true)

  implicit val formats = org.json4s.DefaultFormats

  override def format(loggingEvent: LoggingEvent): String = {
    Try {
      val opsEvent: SimplanOpsEvent = JacksonJsonMapper.fromJson(loggingEvent.getRenderedMessage, classOf[SimplanOpsEvent])
      val eventLevel = eventLevelMapper(loggingEvent.getLevel)
      val updatedLogEvent = opsEvent
        .setError(getExceptionInfo(loggingEvent).orNull)
        .setTimestamp(Instant.now)
      setMDCValues(updatedLogEvent, loggingEvent)
      JacksonJsonMapper.toJson(updatedLogEvent)
    }.getOrElse(StringUtils.EMPTY)
  }

  private def eventLevelMapper(level: Level): EventLevel = {
    level match {
      case Level.ALL   => EventLevel.TRACE
      case Level.TRACE => EventLevel.TRACE
      case Level.DEBUG => EventLevel.DEBUG
      case Level.INFO  => EventLevel.INFORMATIONAL
      case Level.WARN  => EventLevel.IMPORTANT
      case Level.ERROR => EventLevel.VERY_IMPORTANT
    }
  }

  private def setMDCValues(opsEvent: SimplanOpsEvent, loggingEvent: LoggingEvent): Unit = {
    val mdc: Map[String, String] = loggingEvent.getProperties.asScala.map(each => (each._1.toString, each._2.toString)).toMap
    Try {
      val context = Option(opsEvent.context).getOrElse(new ContextOpsEvent())
      context.setLevel(eventLevelMapper(loggingEvent.getLevel))
      mdc.get(MdcConstants.RUN_ID).map(context.setRunId)
      mdc.get(MdcConstants.SOURCE_APP).map(context.setSource)
      mdc.get(MdcConstants.APP_NAME).map(context.setAppName)
      mdc.get(MdcConstants.PARENT_APP_NAME).map(context.setParentName)
      mdc.get(MdcConstants.ENVIRONMENT).map(context.setEnvironment)
      mdc.get(MdcConstants.ORCHESTRATOR).map(context.setOrchestrator)
      mdc.get(MdcConstants.ORCHESTRATOR_ID).map(context.setOrchestratorId)
      opsEvent.setContext(context)
      val meta = Option(opsEvent.meta).getOrElse(new MetaOpsEvent())
      mdc.get(MdcConstants.OPS_OWNER).map(meta.setOpsOwner)
      mdc.get(MdcConstants.BUSINESS_OWNER).map(meta.setBusinessOwner)
      mdc.get(MdcConstants.ASSET_ID).map(meta.setAsset)
      opsEvent.setMeta(meta)
    }

  }

  private def getExceptionInfo(loggingEvent: LoggingEvent): Option[ErrorOpsEvent] = Try(new ErrorOpsEvent(loggingEvent.getThrowableInformation.getThrowable)).toOption

  override def ignoresThrowable(): Boolean = true

  override def activateOptions(): Unit = {}

}
