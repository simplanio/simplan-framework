package com.intuit.data.simplan.parser.sql

import com.intuit.data.simplan.parser.grammer.splitter.{StatementSplitterBaseVisitor, StatementSplitterParser}

import scala.collection.mutable.ListBuffer

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 08-Oct-2021 at 11:13 AM
  */
class StatementSplitterVisitor extends StatementSplitterBaseVisitor[String] {

  private val _statements = new ListBuffer[String]()

  def statements: List[String] = _statements.toList

  override def visitStatement(ctx: StatementSplitterParser.StatementContext): String = {
    _statements += ctx.getText
    super.visitStatement(ctx)
  }
}
