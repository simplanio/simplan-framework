package com.intuit.data.simplan.parser.combinators

import scala.util.Try
import scala.util.parsing.combinator.JavaTokenParsers

/** @author Abraham, Thomas - tabraham1
  *         Created on 19-Oct-2021 at 5:25 PM
  */
class BaseCombinatorParser extends JavaTokenParsers with Serializable {

  def booleanValue: Parser[Boolean] = "true" ^^ (_ => true) | "false" ^^ (_ => false)
  def number: Parser[Double] = floatingPointNumber ^^ (_.toDouble)
  def value: Parser[Any] = stringLiteral ^^ (_.replaceAll("\"", "")) | number | booleanValue
  def valueSequence: Parser[List[Any]] = repsep(value, ",")
  def stringVar: Parser[String] = "[a-zA-Z0-9_]+".r
  def functionName: Parser[String] = "[a-zA-Z]+".r
  def comparator: Parser[String] = "==" | "!=" | ">=" | "<=" | "<" | ">"
  def operator: Parser[String] = "+" | "*" | "-" | "/"

  //<editor-fold desc="Boolean Expressions">
  def or: Parser[Boolean => Boolean] = "or" ~ booleanCheck ^^ { case "or" ~ b => _ || b }
  def and: Parser[Boolean => Boolean] = "and" ~ booleanCheck ^^ { case "and" ~ b => _ && b }
  def booleanOperations: Parser[Boolean] = booleanCheckSequence | "(" ~> booleanCheckSequence <~ ")"
  def booleanCheckSequence: Parser[Boolean] = booleanCheck ~ rep(or | and) ^^ { case a ~ b => (a /: b)((acc, f) => f(acc)) }
  def booleanCheck: Parser[Boolean] = numberComparisons
  private def numberComparisons: Parser[Boolean] = expr ~ comparator ~ expr ^^ { case num1 ~ comp ~ num2 => ParserUtils.numberComparisons(num1, comp, num2) }
  //</editor-fold>

  //<editor-fold desc="Arithmetic Expressions">
  //https://stackoverflow.com/questions/11533547/operator-precedence-with-scala-parser-combinators
  def expr: Parser[Double] = term ~ rep(plus | minus) ^^ { case a ~ b => (a /: b)((acc, f) => f(acc)) }
  def plus: Parser[Double => Double] = "+" ~ term ^^ { case "+" ~ b => _ + b }
  def minus: Parser[Double => Double] = "-" ~ term ^^ { case "-" ~ b => _ - b }
  def term: Parser[Double] = factor ~ rep(times | divide) ^^ { case a ~ b => (a /: b)((acc, f) => f(acc)) }
  def times: Parser[Double => Double] = "*" ~ factor ^^ { case "*" ~ b => _ * b }
  def divide: Parser[Double => Double] = "/" ~ factor ^^ { case "/" ~ b => _ / b }
  def factor: Parser[Double] = fpn | "(" ~> expr <~ ")"
  def fpn: Parser[Double] = floatingPointNumber ^^ (_.toDouble)
  //</editor-fold>

  def numericOperations: Parser[Double] = expr

  def splitOperators: Parser[(String, String, String)] =
    stringVar ~ comparator ~ stringVar ^^ {
      case left ~ comparator ~ right => (left, comparator, right)
    }

  def qualifiedString: Parser[(String, String)] =
    functionName ~ "(" ~ stringVar ~ ")" ^^ {
      case functionName ~ _ ~ dataframeName ~ _ => (functionName, dataframeName)
    }
}

object BaseCombinatorParser {
  private val baseParser = new BaseCombinatorParser()

  def parseQualifiedString(expression: Option[String]): Option[(String, String)] = if (expression.isDefined) Try(baseParser.parseAll(baseParser.qualifiedString, expression.get).get).toOption else None
  def parseQualifiedString(expression: String): Option[(String, String)] = parseQualifiedString(Option(expression))
}
