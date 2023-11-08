package com.intuit.data.simplan.core.util

import com.intuit.data.simplan.common.config._
import com.intuit.data.simplan.common.config.parser.TypesafeConfigUtils
import com.intuit.data.simplan.common.enums.SqlStatementExecutorType
import com.intuit.data.simplan.core.domain.TableType
import com.intuit.data.simplan.core.domain.operator.config.transformations.SqlStatementConfig
import com.intuit.data.simplan.global.utils.SimplanImplicits._
import com.intuit.data.simplan.parser.SqlParser
import com.intuit.data.simplan.parser.sql.{QueryType, SqlStatement}
import com.typesafe.config.{Config, ConfigFactory, ConfigValue}
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang3.StringUtils.EMPTY

import scala.collection.immutable.ListMap
import scala.util.matching.Regex

object SqlScriptToConfigConvertor {
  val hiveconfPattern: Regex = "\\$\\{hiveconf:([0-9a-zA-Z-\\._]+)\\}".r
  val TRIPLE_QUOTE = "\"\"\""
  val SUBSTITUTION_IDENTIFIER = "##ToReplace##"
  val TASK_DEFINITION = "taskDefinition"

  def determineOperatorClass(queryType: QueryType): String = {
    queryType match {
      case QueryType.CTAS             => SqlStatementExecutorType.SqlStatementDMLExecutor.name()
      case QueryType.SELECT           => SqlStatementExecutorType.SqlStatementDMLExecutor.name()
      case QueryType.INSERT           => SqlStatementExecutorType.SqlStatementDMLExecutor.name()
      case QueryType.INSERT_OVERWRITE => SqlStatementExecutorType.SqlStatementDMLExecutor.name()
      case QueryType.TRUNCATE         => SqlStatementExecutorType.SqlStatementDDLExecutor.name()
      case QueryType.LOAD_DATA        => SqlStatementExecutorType.SqlStatementDMLExecutor.name()
      case QueryType.USE              => SqlStatementExecutorType.SqlStatementDMLExecutor.name()
      case QueryType.DROP             => SqlStatementExecutorType.SqlStatementDDLExecutor.name()
      case _                          => SqlStatementExecutorType.SqlStatementDDLExecutor.name()
    }
  }

  def convertor(queries: List[SqlStatement]): ListMap[String, AnyRef] = {
    val map = scala.collection.mutable.LinkedHashMap.empty[String, AnyRef]
    queries.foreach(each => {
      val operatorClass: String = determineOperatorClass(each.statementType)
      val taskDefinition = getTaskDefinition(each.statement, operatorClass)
      map += (each.key -> taskDefinition)
    })
    ListMap(map.toList: _*)
  }

  private def getTaskDefinition(query: String, operatorClass: String) = {
    val allHiveConfMatches: Iterator[Regex.Match] = hiveconfPattern.findAllMatchIn(query)
    if (allHiveConfMatches.isEmpty) {
      getTaskDentitionWithoutSubstitution(query, operatorClass)
    } else {
      getTaskDefinitionWithSubstitution(allHiveConfMatches, query, operatorClass)
    }
  }

  private def getTaskDefinitionWithSubstitution(allHiveConfMatches: Iterator[Regex.Match], query: String, operatorClass: String): ConfigValue = {
    val replacedQuery = allHiveConfMatches.foldLeft(query) {
      case (accumulated, currentMatch) =>
        val from = "\\$\\{hiveconf:" + currentMatch.group(1) + "\\}"
        val to = TRIPLE_QUOTE + "\\$\\{" + currentMatch.group(1) + "\\}" + TRIPLE_QUOTE
        accumulated.replaceAll(from, to)
    }
    val tripleQuotedReplacedQuery: String = TRIPLE_QUOTE + replacedQuery + TRIPLE_QUOTE
    val sqlStatementConfig = new SqlStatementConfig(SUBSTITUTION_IDENTIFIER, Option(StringUtils.EMPTY), TableType.NONE)
    val operation = OperatorDefinitionRef(operatorClass, enabled = true, sqlStatementConfig, Map.empty)
    val taskDefinitionString = TaskDefinitionRef(None, operation, None).toJson.replace("\"" + SUBSTITUTION_IDENTIFIER + "\"", tripleQuotedReplacedQuery)
    val config = ConfigFactory.parseString(s"$TASK_DEFINITION:$taskDefinitionString")
    config.getValue(TASK_DEFINITION)
  }

  private def getTaskDentitionWithoutSubstitution(query: String, operatorClass: String): TaskDefinitionRef = {
    val sqlStatementConfig = new SqlStatementConfig(query, Option(StringUtils.EMPTY), TableType.NONE)
    val operation = OperatorDefinitionRef(operatorClass, enabled = true, sqlStatementConfig, Map.empty)
    TaskDefinitionRef(None, operation, None) // to distinguish between different statement types
  }

  def getSimplanTaskConfigurationFromScript(scriptContent: String): Config = {
    val tasks = convertor(SqlParser.splitScript(scriptContent).map(each => each.copy(statement = each.statement.replace("dlprd.", EMPTY))))
    val config = ConfigFactory.empty()
      .withValue("simplan.tasks.order", TypesafeConfigUtils.overrideAnyConfig(tasks.keys.toList))

    tasks.foldLeft(config) {
      (acc, conf) => acc.withValue("simplan.tasks.dag." + conf._1, TypesafeConfigUtils.overrideAnyConfig(conf._2))
    }
  }
}
