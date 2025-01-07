package com.intuit.data.simplan.core.context

import com.fasterxml.jackson.databind.module.SimpleModule
import com.intuit.data.simplan.common.config.OpsMetricsConfig
import com.intuit.data.simplan.common.files.{FileUtils, LocalFileUtils}
import com.intuit.data.simplan.core.aws.AWSAuthType
import com.intuit.data.simplan.core.domain.operator.config.SinkConfig
import com.intuit.data.simplan.core.domain.operator.config.transformations.WindowOperatorConfig
import com.intuit.data.simplan.core.handlers.ResponseValueQualifiedStringHandler
import com.intuit.data.simplan.core.json.{AWSAuthJacksonDeserializer, SinkJacksonDeSerializer, WindowOperatorJacksonDesrializer}
import com.intuit.data.simplan.core.opsmetrics.handlers.{OpsMetricHandler, Slf4JOpsMetricHandler}
import com.intuit.data.simplan.global.json.SimplanJsonMapper
import com.intuit.data.simplan.global.qualifiedstring.QualifiedParameterManager
import org.slf4j.LoggerFactory

import scala.util.Try

/** @author - Abraham, Thomas - tabaraham1
  *         Created on 8/19/21 at 1:06 AM
  */
abstract class AppContext(initContext: InitContext) extends Support {
  private lazy val logger = LoggerFactory.getLogger(classOf[Support])

  addDefaultAppContextConfigFiles(List("common-operator-mappings.conf", "simplan-config-base.conf"))
  addUserAppContextConfigFiles(initContext.userConfigs.toList)
  initContext.configOverrides.foreach(each => addAppContextConfigOverride(each._1, each._2))
  QualifiedParameterManager.registerHandler(new ResponseValueQualifiedStringHandler)

  val simplanModule: SimpleModule = new SimpleModule("SimplanCoreDeSerializers")
    .addDeserializer(classOf[WindowOperatorConfig], new WindowOperatorJacksonDesrializer)
    .addDeserializer(classOf[AWSAuthType], new AWSAuthJacksonDeserializer)
    .addDeserializer(classOf[SinkConfig], new SinkJacksonDeSerializer)
  SimplanJsonMapper.registerModule(simplanModule)

  lazy override val fileUtils: FileUtils = initContext.fileUtils

  lazy val applicationId: String = appContextConfig.application.runId.get

  lazy val opsMetricsEmitter: OpsMetricHandler = {
    val config = Try(appContextConfig.getSystemConfigAs[OpsMetricsConfig]("opsMetrics")).getOrElse(OpsMetricsConfig(cls = classOf[Slf4JOpsMetricHandler].getCanonicalName))
    val handler = Class.forName(config.cls).getConstructor(classOf[AppContext], classOf[OpsMetricsConfig]).newInstance(this, config).asInstanceOf[OpsMetricHandler]
    logger.info(s"Using OpsMetrics Handler : ${handler.getClass.getCanonicalName}")
    handler
  }
}
