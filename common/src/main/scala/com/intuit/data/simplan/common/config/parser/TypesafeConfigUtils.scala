package com.intuit.data.simplan.common.config.parser

import com.intuit.data.simplan.global.json.{JacksonUtil, SimplanJsonMapper}
import com.typesafe.config.{Config, ConfigValue, ConfigValueFactory}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 23-Feb-2022 at 5:39 PM
  */
object TypesafeConfigUtils {

  def getBoolean(config: Option[Config], path: String): Option[Boolean] = config.map(getBoolean(_, path)).get

  def getBoolean(config: Config, path: String): Option[Boolean] = Try(config.getBoolean(path)).toOption

  def getConfig(config: Option[Config], path: String): Option[Config] = config.map(getConfig(_, path)).get

  def getConfig(config: Config, path: String): Option[Config] = Try(config.getConfig(path)).toOption

  def getConfigList(config: Option[Config], path: String): List[Config] = config.map(getConfigList(_, path)).get

  def getConfigList(config: Config, path: String): List[Config] =
    Try(config.getConfigList(path).asScala.toList) match {
      case Success(value) => value
      case Failure(_)     => List.empty
    }

  def getString(config: Option[Config], path: String): Option[String] = config.map(getString(_, path)).get

  def getString(config: Config, path: String): Option[String] = Try(config.getString(path)).toOption

  def overrideAnyConfig(value: Any): ConfigValue = {
    value match {
      case configValue: ConfigValue => configValue
      case string: String           => ConfigValueFactory.fromAnyRef(string)
      case number: Number           => ConfigValueFactory.fromAnyRef(number)
      case boolean: Boolean         => ConfigValueFactory.fromAnyRef(boolean)
      case list: List[_]            => ConfigValueFactory.fromAnyRef(list.asJava)
      //  case map: Map[_, _]           => ConfigValueFactory.fromAnyRef(map.asJava)
      case _ =>
        ConfigValueFactory.fromAnyRef(
          JacksonUtil.fromJsonToMap(SimplanJsonMapper.toJson(value.asInstanceOf[AnyRef]))
        ) //TODO: Need to find a better way for this. This is doing double conversion. Scala class -> Json -> Java util map.
    }
  }
}
