package com.intuit.data.simplan.core.context

import com.intuit.data.simplan.common.config.SimplanAppContextConfiguration
import com.intuit.data.simplan.common.config.parser.TypesafeConfigLoader
import com.intuit.data.simplan.common.emitters.SimplanEmitterRegistry
import com.intuit.data.simplan.common.files.{FileUtils, LocalFileUtils}
import com.intuit.data.simplan.global.utils.IdGenerator
import com.intuit.data.simplan.logging.MdcConstants._
import com.typesafe.config.ConfigValue
import org.slf4j.{LoggerFactory, MDC}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait Support extends Serializable {
  private lazy val logger = LoggerFactory.getLogger(classOf[Support])

  protected val _userAppContextConfigFiles = new ListBuffer[String]
  protected val _defaultAppContextConfigFiles = new ListBuffer[String]

  @transient protected val appContextConfigOverrides = new mutable.HashMap[String, ConfigValue]
  lazy val appInstanceID: String = IdGenerator.randomUUID

  lazy val fileUtils: FileUtils = new LocalFileUtils

  private val _fileUtilsMap = new mutable.HashMap[String, FileUtils]
  def fileUtilsMap: Map[String, FileUtils] = _fileUtilsMap.toMap


  def registerFileUtils(fileUtils: FileUtils): this.type = {
    fileUtils.schemes.foreach(scheme => {
      if (_fileUtilsMap.contains(scheme)) logger.warn(s"FileUtils for scheme $scheme already registered. Overriding with $fileUtils")
      _fileUtilsMap.put(scheme, fileUtils)
    })
    this
  }

  def defaultAppContextConfigFiles: List[String] = _defaultAppContextConfigFiles.toList
  def userAppContextConfigFiles: List[String] = _userAppContextConfigFiles.toList

  lazy val appContextConfig: SimplanAppContextConfiguration = {
    val configuration = loadApplicationConfiguration()
    MDC.put(ASSET_ID, configuration.application.asset)
    MDC.put(ENVIRONMENT, configuration.application.environment)
    MDC.put(APP_NAME, configuration.application.name)
    MDC.put(RUN_ID, configuration.application.runId.get)
    MDC.put(SOURCE_APP, configuration.application.source)
    MDC.put(ORCHESTRATOR, configuration.application.orchestrator)

    if (configuration.application.businessOwner.isDefined) MDC.put(BUSINESS_OWNER, configuration.application.businessOwner.get)
    if (configuration.application.opsOwner.isDefined) MDC.put(OPS_OWNER, configuration.application.opsOwner.get)
    if (configuration.application.parent.isDefined) MDC.put(PARENT_APP_NAME, configuration.application.parent.get)
    if (configuration.application.region.isDefined) MDC.put(REGION, configuration.application.region.get)
    if (configuration.application.orchestratorId.isDefined) MDC.put(ORCHESTRATOR_ID, configuration.application.orchestratorId.get)
    configuration
  }

  lazy val emitters: SimplanEmitterRegistry = SimplanEmitterRegistry.apply(appContextConfig)

  def loadApplicationConfiguration(namespace: String = "simplan"): SimplanAppContextConfiguration = {
    val loader = new TypesafeConfigLoader(namespace, _defaultAppContextConfigFiles.toList, fileUtilsMap)
    _userAppContextConfigFiles.foreach(loader.load)
    appContextConfigOverrides.foreach { case (key, value) => loader.overrideConfig(key, value) }
    TypesafeConfigLoader.resolveSystemConfiguration(loader)
  }

  def addAppContextConfigOverride(fullyQualifiedKey: String, configValue: ConfigValue): Support = {
    appContextConfigOverrides += (fullyQualifiedKey -> configValue)
    this
  }

  def addUserAppContextConfigFiles(configFiles: List[String]): Support = {
    this._userAppContextConfigFiles ++= configFiles
    this
  }

  def addDefaultAppContextConfigFile(configFile: String): Support = {
    _defaultAppContextConfigFiles += configFile
    this
  }

  def addDefaultAppContextConfigFiles(configFiles: List[String]): Support = {
    this._defaultAppContextConfigFiles ++= configFiles
    this
  }

}
