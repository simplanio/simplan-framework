package com.intuit.data.simplan.core.context

import com.intuit.data.simplan.common.config.parser.TypesafeConfigLoader
import com.intuit.data.simplan.common.config.{OperatorDefinitionRef, SimplanTasksConfiguration, TaskDefinitionRef}
import com.intuit.data.simplan.core.domain.OperatorType
import com.intuit.data.simplan.core.domain.operator.OperatorResponse
import com.intuit.data.simplan.core.executor.{DagExecutor, SequentialDagExecutor}
import com.intuit.data.simplan.core.opsmetrics.trackers.ApplicationExecutionTracker
import com.intuit.data.simplan.global.json.{JacksonUtil, SimplanJsonMapper}
import com.intuit.data.simplan.logging.Logging
import com.typesafe.config.{ConfigValue, ConfigValueFactory}

import java.time.Instant
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/** @author - Abraham, Thomas - tabaraham1
 *          Created on 8/19/21 at 1:06 AM
 */
class Application(val appContext: AppContext) extends Serializable with Logging {
  @transient private val _configOverrides = new mutable.HashMap[String, ConfigValue]

  private val startupTime: Instant = Instant.now()

  protected val userTaskConfigFiles = new ListBuffer[String]
  protected val defaultTaskConfigFiles = new ListBuffer[String]

  private val appExecutionTracker = ApplicationExecutionTracker(appContext, appContext.appContextConfig.application.name, Option(startupTime))

  private def resolvedTaskConfigFiles: List[String] = defaultTaskConfigFiles.toList ++ userTaskConfigFiles.toList

  lazy private val (defaultFiles, userFiles) =
    if (resolvedTaskConfigFiles.isEmpty)
      (appContext.defaultAppContextConfigFiles, appContext.userAppContextConfigFiles)
    else
      (appContext.defaultAppContextConfigFiles ++ defaultTaskConfigFiles.toList, appContext.userAppContextConfigFiles ++ userTaskConfigFiles.toList)

  lazy val taskConfigs: SimplanTasksConfiguration = {
    val loader = new TypesafeConfigLoader("simplan", defaultFiles, appContext.fileUtilsMap)
    userFiles.foreach(loader.load)
    _configOverrides.foreach { case (key, value) => loader.overrideConfig(key, value) }
    TypesafeConfigLoader.resolveTaskConfiguration(loader)
  }

  lazy val executor: DagExecutor = new SequentialDagExecutor(appContext, taskConfigs)

  final def run[T <: Serializable](runParameters: T): Boolean = {
    Try(executor.execute[T](runParameters)) match {
      case Success(value) => appExecutionTracker.success(); value
      case Failure(exception) => appExecutionTracker.failed(s"Application Execution failed : ${exception.getMessage}", Some(exception)); throw exception
    }
  }

  final def simulate[T <: Serializable](runParameters: T, taskName: String): Map[String, OperatorResponse] = {
    Try(executor.simulate[T](runParameters, taskName)) match {
      case Success(value) => appExecutionTracker.success(); value
      case Failure(exception) => appExecutionTracker.failed(s"Application Simulation failed : ${exception.getMessage}", Some(exception)); throw exception
    }
  }

  final def run(): Boolean = run[DefaultRunParameters](new DefaultRunParameters())

  final def simulate(taskName: String): Map[String, OperatorResponse] = simulate[DefaultRunParameters](new DefaultRunParameters(), taskName)

  def stop: Boolean = ???

  def addConfigs(configs: List[String]): Application = withConfigs(configs)

  def addConfigs(configs: String*): Application = withConfigs(configs: _*)

  def withConfigs(configs: List[String]): Application = {
    this.userTaskConfigFiles ++= configs;
    this
  }

  def withConfigs(configs: String*): Application = {
    this.userTaskConfigFiles ++= configs;
    this
  }

  def configOverrides: Map[String, ConfigValue] = _configOverrides.toMap

  def overrideTask(taskName: String, definition: TaskDefinitionRef): Application = overrideAnyConfig("simplan.tasks.dag." + taskName, definition)

  def overrideTask(taskName: String, definition: AnyRef): Application = overrideAnyConfig("simplan.tasks.dag." + taskName, definition)

  def startFromTask(taskName: String): Application = if (Option(taskName).isDefined) overrideAnyConfig("simplan.tasks.startingTask", taskName) else this

  def startFromTask(taskName: Option[String]): Application = if (taskName.isDefined) overrideAnyConfig("simplan.tasks.startingTask", taskName.get) else this

  def overrideTasks(tasks: Map[String, AnyRef]): Application = {
    tasks.foreach { case (taskName, taskDefinition) => overrideAnyConfig(s"simplan.tasks.dag.$taskName", taskDefinition) }
    this
  }

  def overrideTaskOrder(taskOrder: List[String]): Application = overrideAnyConfig("simplan.tasks.order", taskOrder)

  def overrideOperator(taskName: String, operatorType: OperatorType, definition: OperatorDefinitionRef): Application =
    overrideAnyConfig(s"simplan.tasks.dag.$taskName.${operatorType.getName}", definition)

  def overrideAnyConfig(fullyQualifiedKey: String, value: Any): Application = {
    logger.debug(s"Applying Task config $fullyQualifiedKey : $value")
    val configValue: ConfigValue = value match {
      case configValue: ConfigValue => configValue
      case string: String => ConfigValueFactory.fromAnyRef(string)
      case number: Number => ConfigValueFactory.fromAnyRef(number)
      case boolean: Boolean => ConfigValueFactory.fromAnyRef(boolean)
      case list: List[_] => ConfigValueFactory.fromAnyRef(list.asJava)
      //  case map: Map[_, _]           => ConfigValueFactory.fromAnyRef(map.asJava)
      case _ =>
        ConfigValueFactory.fromAnyRef(
          JacksonUtil.fromJsonToMap(SimplanJsonMapper.toJson(value.asInstanceOf[AnyRef]))
        ) //TODO: Need to find a better way for this. This is doing double conversion. Scala class -> Json -> Java util map.
    }
    _configOverrides += (fullyQualifiedKey -> configValue)
    this
  }
}
