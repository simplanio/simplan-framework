package com.intuit.data.simplan.common.config.parser

import com.intuit.data.simplan.common.config.parser.TypesafeConfigUtils.getString
import com.intuit.data.simplan.common.config.{SimplanAppContextConfiguration, SimplanTasksConfiguration}
import com.intuit.data.simplan.common.exceptions.SimplanConfigException
import com.intuit.data.simplan.common.files.FileUtils
import com.intuit.data.simplan.global.exceptions.SimplanException
import com.intuit.data.simplan.global.utils.IdGenerator
import com.intuit.data.simplan.logging.Logging
import com.typesafe.config._
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import pureconfig._
import pureconfig.error.ConfigReaderFailures

import java.io.File
import java.net.URI
import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Try

class TypesafeConfigLoader(val namespace: String, defaultFiles: List[String], fileUtilsMap: Map[String, FileUtils]) {

  @transient private lazy val logger: Logger = getLogger(this.getClass)
  val configArray: ListBuffer[Config] = new scala.collection.mutable.ListBuffer[Config]()
  @transient protected val configOverrides = new mutable.HashMap[String, ConfigValue]

  // First parse default configuration
  logger.info(s"Default Config Files Used : $defaultFiles")
  @transient private lazy val defaultConfig: Config = mergeConfigFiles(defaultFiles.filter(_.nonEmpty).map(ConfigFactory.parseResources), ConfigFactory.empty())

  def loadFile(file: File): Unit = configArray += ConfigFactory.parseFile(file)

  def load(file: String): TypesafeConfigLoader = {
    val configParseOptions = ConfigParseOptions.defaults.setAllowMissing(false) //.setIncluder(new TypesafeObjectStorageIncluder())
    val config = file.trim match {
      case r if r.startsWith("classpath:") => ConfigFactory.parseResources(new URI(file).getSchemeSpecificPart, configParseOptions)
      case r if r.startsWith("file:")      => ConfigFactory.parseFile(new File(new URI(file).getSchemeSpecificPart), configParseOptions)
      case r if r.startsWith("/")          => ConfigFactory.parseFile(new File(file), configParseOptions)
      case r if isSupportedScheme(r) =>
        val fileUtilsForScheme = fileUtilsMap(r)
        logger.info(s"Loading file from fileUtilsMap : $r using FileUtils : $fileUtilsForScheme ")
        ConfigFactory.parseString(fileUtilsForScheme.readContent(r), configParseOptions)
      case _ => ConfigFactory.parseString(file, configParseOptions)
    }
    configArray += config
    this
  }

  def getFileUtils(scheme: String): FileUtils = fileUtilsMap.getOrElse(scheme, throw new SimplanException(s"FileUtils for scheme $scheme not registered"))

  def isSupportedScheme(file: String): Boolean = Try {
    val scheme: String = new URI(file).getScheme
    fileUtilsMap.contains(scheme)
  }.toOption.get

  def overrideConfig(fullyQualifiedKey: String, configValue: ConfigValue): TypesafeConfigLoader = {
    configOverrides += (fullyQualifiedKey -> configValue)
    this
  }

  @transient private def resolveConfig(): Config = {
    logger.info("Loading System Properties")
    val configsWithSystemProperties = configArray.toList ++ List(ConfigFactory.systemProperties())
    val config: Config = mergeConfigFiles(configsWithSystemProperties, defaultConfig)
    val configWithOverrides = configOverrides.foldLeft(config)((a, n) => a.withValue(n._1, n._2))
    Try(attachInstanceId(configWithOverrides)).getOrElse(configWithOverrides).resolve()
  }

  def attachInstanceId(config: Config): Config = {
    if (getString(config.getConfig("simplan.application"), "runId").isEmpty) {
      config.withValue("simplan.application.runId", ConfigValueFactory.fromAnyRef(IdGenerator.randomUUID))
    } else config
  }

  @tailrec
  private def mergeConfigFiles(configs: List[Config], fallbackConfig: Config): Config =
    if (configs.isEmpty) fallbackConfig
    else mergeConfigFiles(configs.tail, configs.head.withFallback(fallbackConfig))

  def render(key: Option[String] = Some(namespace), renderOption: ConfigRenderOptions = ConfigRenderOptions.concise()): String = {
    val resolvedConfig: Config = if (key.isDefined) resolveConfig().getConfig(key.get) else resolveConfig()
    resolvedConfig.root().render(renderOption.setJson(false).setFormatted(true))
  }

}

object TypesafeConfigLoader extends Logging {
  def apply(namespace: String, defaultFile: String, fileUtilsMap: Map[String, FileUtils]) = new TypesafeConfigLoader(namespace: String, List(defaultFile), fileUtilsMap)
  implicit def hint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  def resolveSystemConfiguration(parser: TypesafeConfigLoader): SimplanAppContextConfiguration = {
    logger.info("Trying to load AppContext Configs")
    val config = parser.resolveConfig()
    val configuration = pureconfig.loadConfig[SimplanAppContextConfiguration](config.getConfig(parser.namespace)) match {
      case Right(renderedConfig: SimplanAppContextConfiguration) => renderedConfig
      case Left(failures: ConfigReaderFailures)                  => throw new SimplanConfigException(failures.toString)
    }
    logger.info("AppContext Configs : " + parser.render(Option(parser.namespace)))
    logger.info("Successfully loaded AppContext Configs")
    configuration
  }

  def resolveTaskConfiguration(parser: TypesafeConfigLoader): SimplanTasksConfiguration = {
    logger.info("Trying to load Application Dag Configs")
    val config = parser.resolveConfig()
    val configuration = pureconfig.loadConfig[SimplanTasksConfiguration](config.getConfig(parser.namespace)) match {
      case Right(renderedConfig: SimplanTasksConfiguration) => renderedConfig
      case Left(failures: ConfigReaderFailures)             => throw new SimplanConfigException(failures.toString)
    }
    logger.info("Application Dag Configs : " + parser.render(Option(parser.namespace)))
    logger.info("Successfully loaded Application Dag Configs")

    configuration
  }
}
