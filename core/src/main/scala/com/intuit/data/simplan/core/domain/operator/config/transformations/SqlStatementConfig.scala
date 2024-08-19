package com.intuit.data.simplan.core.domain.operator.config.transformations

import com.intuit.data.simplan.core.domain.TableType
import com.intuit.data.simplan.core.domain.operator.OperatorConfig

/** @author Abraham, Thomas - tabraham1
  *         Created on 18-Nov-2021 at 9:36 PM
  */
//case class SqlStatementConfig(sql: String, table: String, tableType: TableType = TableType.NONE) extends Serializable
class SqlStatementConfig(
    val sql: String,
    val table: Option[String],
    val tableType: TableType = TableType.NONE,
    val tableFormat: Option[String] = None
) extends OperatorConfig {
  def resolvedTableFormat: String = tableFormat.getOrElse("parquet")
}
