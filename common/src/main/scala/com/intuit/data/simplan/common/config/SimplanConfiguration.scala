package com.intuit.data.simplan.common.config

import com.intuit.data.simplan.common.exceptions.SimplanConfigException
import com.intuit.data.simplan.global.json.SimplanJsonMapper

case class SimplanAppContextConfiguration(
                                           application: ApplicationConfig,
                                           system: SystemConfig,
                                           features: Map[String, Boolean] = Map.empty,
                                           emitters: Map[String, SimplanEmitterConfig] = Map.empty,
                                           variables: Map[String, String] = Map.empty
                                         ) extends Serializable {

  def getSystemConfigAs[T](key: String)(implicit m: Manifest[T]): T =
    system.config.get(key) match {
      case Some(config) => SimplanJsonMapper.fromJson[T](config)
      case None => throw new SimplanConfigException(s"Simplan System configuration for '$key' not found. Please add configuration under 'simplan.system.config.$key'")
    }

}

case class SimplanTasksConfiguration(
                                      tasks: TasksConfig,
                                      metadata: Map[String, String] = Map.empty
                                    ) extends Serializable

case class TasksConfig(
                        startingTask: Option[String],
                        order: List[String],
                        dag: Map[String, TaskDefinition]
                      ) extends Serializable

case class ApplicationConfig(
                              name: String,
                              asset: String,
                              environment: String,
                              orchestrator: String,
                              orchestratorId: Option[String],
                              source: String,
                              qualifiedInstanceId: String = "",
                              namespace: String = "default",
                              configHome: String = System.getProperty("java.io.tmpdir"),
                              runId: Option[String],
                              parent: Option[String],
                              region: Option[String],
                              opsOwner: Option[String],
                              businessOwner: Option[String],
                              operatorMappings: Map[String, String]
                            ) extends Serializable

case class SystemConfig(config: Map[String, String] = Map.empty) extends Serializable

case class TaskDefinition(trigger: Option[OperatorDefinition], action: OperatorDefinition, validation: Option[OperatorDefinition])

case class TaskDefinitionRef(trigger: Option[OperatorDefinitionRef], action: OperatorDefinitionRef, validation: Option[OperatorDefinitionRef])

case class OperatorDefinition(operator: String, enabled: Boolean = true, config: String = "{}", options: String = "{}")

case class OperatorDefinitionRef(operator: String, enabled: Boolean = true, config: AnyRef, options: AnyRef)

case class SimplanEmitterConfig(handler: String, enabled: Option[Boolean] = Some(false), config: String = "{}")
