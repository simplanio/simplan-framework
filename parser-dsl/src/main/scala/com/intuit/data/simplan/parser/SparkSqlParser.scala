package com.intuit.data.simplan.parser

import com.intuit.data.simplan.parser.errors.SyntaxErrorListener
import com.intuit.data.simplan.parser.grammer.sparksql.{SparkSqlBaseLexer, SparkSqlBaseParser}
import com.intuit.data.simplan.parser.sql._
import com.intuit.data.simplan.parser.sql.catalog.MetastoreCatalogProvider
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}

import java.util

object SparkSqlParser {

  def isValidSql(sql: String): Boolean = {
    val sparkCharStream = CharStreams.fromString(sql)
    val upper = new CaseChangingCharStream(sparkCharStream, true)
    val sparkLexer = new SparkSqlBaseLexer(upper);
    val sparkTokens = new CommonTokenStream(sparkLexer);
    val sparkParser = new SparkSqlBaseParser(sparkTokens);

    val listener = new SyntaxErrorListener
    sparkParser.addErrorListener(listener)
    if (listener.syntaxErrors.length >= 1) {
      return false
    }
    true
  }

  def isValidMultilineSql(sql: String): util.ArrayList[String] = {
    val result = new util.ArrayList[String]()
    val listOfSqlStatements = SqlParser.splitScript(sql)
    listOfSqlStatements.foreach(sqlStatement => {
      val statement = sqlStatement.statement.replace("dlprd.", "")
      val sparkCharStream = CharStreams.fromString(statement)
      val upper = new CaseChangingCharStream(sparkCharStream, true)
      val sparkLexer = new SparkSqlBaseLexer(upper);
      val sparkTokens = new CommonTokenStream(sparkLexer);
      val sparkParser = new SparkSqlBaseParser(sparkTokens);
      val listener = new SyntaxErrorListener
      sparkParser.addErrorListener(listener)
      if (listener.syntaxErrors.nonEmpty) {
        val input = listener.syntaxErrors.head.productElement(4).toString
        result.add(input)
      }
    })
    result
  }

  def parse(sql: String, catalog: MetastoreCatalogProvider): Statement = {
    val sparkCharStream = CharStreams.fromString(sql)
    val upper = new CaseChangingCharStream(sparkCharStream, true)
    val sparkLexer = new SparkSqlBaseLexer(upper);
    val sparkTokens = new CommonTokenStream(sparkLexer);
    val sparkParser = new SparkSqlBaseParser(sparkTokens);

    val listener = new SyntaxErrorListener
    sparkParser.addErrorListener(listener)
    val visitor = new SparkSqlQueryParserVisitor(catalog)
    val statement = sparkParser.statement()
    val parsedStmt: Statement = visitor.visit(statement).asInstanceOf[Statement]
    parsedStmt
  }
}
