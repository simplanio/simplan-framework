package com.intuit.data.simplan.core.opsmetrics

import com.intuit.data.simplan.core.context.AppContext
import com.intuit.data.simplan.logging.domain.v2.SimplanOpsEvent
import com.intuit.data.simplan.logging.domain.v2.fiedsets.{ContextOpsEvent, ErrorOpsEvent, EventLevel, MetaOpsEvent}

import java.time.Instant
import scala.util.Try

/** @author Abraham, Thomas - tabraham1
  *         Created on 12-Nov-2021 at 9:13 AM
  */
object OpsEventFormatter {

  def format(appContext: AppContext, opsEvent: SimplanOpsEvent, level: EventLevel = EventLevel.IMPORTANT, throwable: Option[Throwable] = None): SimplanOpsEvent = {
    val eventLevel = level
    val updatedLogEvent = opsEvent
      .setError(getExceptionInfo(throwable).orNull)
      .setTimestamp(Instant.now)
    setContext(updatedLogEvent, appContext, eventLevel);
    updatedLogEvent
  }

  private def setContext(opsEvent: SimplanOpsEvent, appContext: AppContext, level: EventLevel = EventLevel.IMPORTANT): Unit = {
    Try {
      val context: ContextOpsEvent = Option(opsEvent.context).getOrElse(new ContextOpsEvent())
      context.setLevel(level)
      context.setNamespace(appContext.appContextConfig.application.namespace)
      context.setRunId(appContext.appContextConfig.application.runId.orNull)
      context.setSource(appContext.appContextConfig.application.source)
      context.setAppName(appContext.appContextConfig.application.name)
      context.setParentName(appContext.appContextConfig.application.parent.orNull)
      context.setEnvironment(appContext.appContextConfig.application.environment)
      context.setApplicationId(appContext.applicationId)
      context.setOrchestrator(appContext.appContextConfig.application.orchestrator)
      context.setApplicationId(appContext.appContextConfig.application.orchestratorId.orNull)
      context.setInstanceId(appContext.appContextConfig.application.qualifiedInstanceId)
      opsEvent.setContext(context)

      val meta = Option(opsEvent.meta).getOrElse(new MetaOpsEvent())
      meta.setOpsOwner(appContext.appContextConfig.application.opsOwner.orNull)
      meta.setBusinessOwner(appContext.appContextConfig.application.businessOwner.orNull)
      meta.setAsset(appContext.appContextConfig.application.asset)
      opsEvent.setMeta(meta)
    }

  }

  private def getExceptionInfo(throwable: Option[Throwable]): Option[ErrorOpsEvent] = Try(new ErrorOpsEvent(throwable.orNull)).toOption

}
