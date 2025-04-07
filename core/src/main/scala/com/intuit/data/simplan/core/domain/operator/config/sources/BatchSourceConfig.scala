package com.intuit.data.simplan.core.domain.operator.config.sources

import com.intuit.data.simplan.core.domain.TableType
import com.intuit.data.simplan.core.domain.operator.OperatorConfig
import com.intuit.data.simplan.global.domain.QualifiedParam

/** @author Abraham, Thomas - tabraham1
  *         Created on 17-Nov-2021 at 2:57 PM
  */
class BatchSourceConfig(
    val location: String,
    val format: String,
    val schema: Option[QualifiedParam],
    val tableType: TableType = TableType.NONE,
    val table: Option[String] = None,
    val projection: List[String] = List.empty,
    val filter: Option[String] = None,
    val options: Map[String, String] = Map.empty,
    val directorySortPattern: Option[String] = None)
    extends OperatorConfig {
  def resolvedOptions: Map[String, String] = if (Option(options).isDefined) options else Map.empty[String, String]
  def resolvedProjections: List[String] = if (Option(projection).isDefined) projection else List.empty[String]

}
