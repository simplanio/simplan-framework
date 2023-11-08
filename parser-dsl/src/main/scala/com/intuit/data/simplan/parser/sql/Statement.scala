package com.intuit.data.simplan.parser.sql

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 16-Aug-2022 at 5:00 PM
  */
case class SqlStatement(statementType: QueryType, key: String, statement: String) extends Serializable
