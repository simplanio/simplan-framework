package com.intuit.data.simplan.core.domain

case class Lineage(statement: String, inputObjects: List[String], outputObject: Option[String])

object Lineage {
  val TASK_NAME = "PublishLineageToSuperglue"
  val RESPONSE_VALUE_KEY = "lineage"
  def apply(statement: String): Lineage = Lineage(statement, List.empty, None)
}
