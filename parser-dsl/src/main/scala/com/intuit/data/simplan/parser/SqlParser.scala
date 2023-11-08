package com.intuit.data.simplan.parser

import com.intuit.data.simplan.parser.errors.{SyntaxError, SyntaxErrorListener}
import com.intuit.data.simplan.parser.grammer.presto.{SqlBaseLexer, SqlBaseParser}
import com.intuit.data.simplan.parser.grammer.sparksql.{SparkSqlBaseLexer, SparkSqlBaseParser}
import com.intuit.data.simplan.parser.grammer.splitter.{StatementSplitterLexer, StatementSplitterParser}
import com.intuit.data.simplan.parser.sql.{QueryType, SqlClassifier, SqlStatement, StatementSplitterVisitor}
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}

import java.io.File
import scala.collection.JavaConverters._
import scala.io.Source

object SqlParser {

  def splitScript(script: String): List[SqlStatement] = {
    val stream = CharStreams.fromString(script)
    val lexer = new StatementSplitterLexer(stream);
    val tokens = new CommonTokenStream(lexer);
    val parser = new StatementSplitterParser(tokens);
    val tree = parser.statements()
    val walker = new StatementSplitterVisitor();
    walker.visit(tree)
    val queries = walker.statements.map(_.trim).filter(_.nonEmpty)
    queries
      .filter(query => query != null && query.trim.nonEmpty)
      .map(_.trim)
      .zipWithIndex
      .map {
        case (query, index) =>
          val (stepName, queryType): (String, QueryType) = SqlClassifier.classify(query, index)
          SqlStatement(queryType, stepName, query)
      }
  }

  def splitScript(sqlScript: File): List[SqlStatement] = {
    val bufferedSource = Source.fromFile(sqlScript)
    val fileContents: String = bufferedSource.getLines.mkString("\n")
    bufferedSource.close
    splitScript(fileContents)
  }
  def splitScriptJava(sqlScript: File): java.util.List[SqlStatement] = splitScript(sqlScript).asJava
  def splitScriptJava(script: String): java.util.List[SqlStatement] = splitScript(script).asJava

  def validateSparkSql(statement: String): Either[Boolean, List[SyntaxError]] = {
    val sparkCharStream = CharStreams.fromString(statement)
    val upper = new CaseChangingCharStream(sparkCharStream, true)
    val sparkLexer = new SparkSqlBaseLexer(upper);
    val sparkTokens = new CommonTokenStream(sparkLexer);
    val sparkParser = new SparkSqlBaseParser(sparkTokens);

    val listener = new SyntaxErrorListener
    sparkParser.addErrorListener(listener)
    if (listener.syntaxErrors.nonEmpty) {
      Right(listener.syntaxErrors)
    } else Left(true)
  }

  def validatePrestoSql(statement: String): Either[Boolean, List[SyntaxError]] = {
    val sparkCharStream = CharStreams.fromString(statement)
    val upper = new CaseChangingCharStream(sparkCharStream, true)
    val sparkLexer = new SqlBaseLexer(upper);
    val sparkTokens = new CommonTokenStream(sparkLexer);
    val sparkParser = new SqlBaseParser(sparkTokens);

    val listener = new SyntaxErrorListener
    sparkParser.addErrorListener(listener)
    if (listener.syntaxErrors.nonEmpty) {
      Right(listener.syntaxErrors)
    } else Left(true)
  }

}
