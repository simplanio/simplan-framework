package com.intuit.data.simplan.core.context

import com.intuit.data.simplan.common.config.{OperatorDefinitionRef, TaskDefinitionRef}
import com.intuit.data.simplan.common.files.FileUtils
import com.intuit.data.simplan.core.domain.OperatorType
import com.intuit.data.simplan.global.json.{JacksonUtil, SimplanJsonMapper}
import com.intuit.data.simplan.logging.Logging
import com.typesafe.config.{ConfigValue, ConfigValueFactory}

import scala.collection.JavaConverters.asJavaIterableConverter
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/** @author - Abraham, Thomas - tabaraham1
  *         Created on 8/19/21 at 1:08 AM
  */
class InitContext(_userConfigs: Array[String] = Array.empty) extends Serializable with Logging {
  @transient private val _configOverrides = new mutable.HashMap[String, ConfigValue]
  private val _additionalUserConfigs: ListBuffer[String] = new mutable.ListBuffer[String]()

  def userConfigs: Array[String] = _userConfigs ++ _additionalUserConfigs.toList

  def addAdditionalUserConfig(config: String): InitContext = {
    _additionalUserConfigs += config
    this
  }

  def configOverrides: Map[String, ConfigValue] = _configOverrides.toMap
  def overrideTask(taskName: String, definition: TaskDefinitionRef): InitContext = overrideAnyConfig("simplan.dag." + taskName, definition)
  def appName(appName: String): InitContext = overrideAnyConfig("simplan.application.name", appName)
  def parentAppName(pipelineName: String): InitContext = overrideAnyConfig("simplan.application.pipeline", pipelineName)
  def overrideTask(taskName: String, definition: AnyRef): InitContext = overrideAnyConfig("simplan.dag." + taskName, definition)
  def overrideTaskOrder(taskOrder: List[String]): InitContext = overrideAnyConfig("simplan.application.taskOrder", taskOrder)
  def overrideOperator(taskName: String, operatorType: OperatorType, definition: OperatorDefinitionRef): InitContext = overrideAnyConfig(s"simplan.dag.$taskName.${operatorType.getName}", definition)
  def overrideSystemConfig(systemName: String, systemConfig: Any): InitContext = overrideAnyConfig("simplan.system.config." + systemName, systemConfig)

  def overrideAnyConfig(fullyQualifiedKey: String, value: Any): InitContext = {
    logger.debug(s"Applying config $fullyQualifiedKey : $value")
    val configValue: ConfigValue = value match {
      case configValue: ConfigValue => configValue
      case string: String           => ConfigValueFactory.fromAnyRef(string)
      case number: Number           => ConfigValueFactory.fromAnyRef(number)
      case boolean: Boolean         => ConfigValueFactory.fromAnyRef(boolean)
      case list: List[_]            => ConfigValueFactory.fromAnyRef(list.asJava)
      case _                        => ConfigValueFactory.fromAnyRef(JacksonUtil.fromJsonToMap(SimplanJsonMapper.toJson(value.asInstanceOf[AnyRef]))) //TODO: Need to find a better way for this. This is doing double conversion. Scala class -> Json -> Java util map.
    }
    _configOverrides += (fullyQualifiedKey -> configValue)
    this
  }
}

object InitContext {
  def apply(userConfigs: Array[String]): InitContext = new InitContext(userConfigs)
}
