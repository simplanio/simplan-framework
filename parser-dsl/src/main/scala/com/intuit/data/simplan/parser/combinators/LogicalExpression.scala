package com.intuit.data.simplan.parser.combinators

import scala.util.parsing.combinator.JavaTokenParsers

/**
  *  <b-expression>::= <b-term> [<orop> <b-term>]*
  *  <b-term>      ::= <not-factor> [AND <not-factor>]*
  *  <not-factor>  ::= [NOT] <b-factor>
  *  <b-factor>    ::= <b-literal> | <b-variable> | (<b-expression>)
  */

case class LogicalExpression(variableMap: Map[String, Boolean]) extends JavaTokenParsers {
  private lazy val b_expression: Parser[Boolean] = b_term ~ rep("or" ~ b_term) ^^ { case f1 ~ fs ⇒ (f1 /: fs)(_ || _._2) }
  private lazy val b_term: Parser[Boolean] = (b_not_factor ~ rep("and" ~ b_not_factor)) ^^ { case f1 ~ fs ⇒ (f1 /: fs)(_ && _._2) }
  private lazy val b_not_factor: Parser[Boolean] = opt("not") ~ b_factor ^^ (x ⇒ x match { case Some(v) ~ f ⇒ !f; case None ~ f ⇒ f })
  private lazy val b_factor: Parser[Boolean] = b_literal | b_variable | ("(" ~ b_expression ~ ")" ^^ { case "(" ~ exp ~ ")" ⇒ exp })
  private lazy val b_literal: Parser[Boolean] = "true" ^^ (x ⇒ true) | "false" ^^ (x ⇒ false)
  // This will construct the list of variables for this parser
  private lazy val b_variable: Parser[Boolean] = variableMap.keysIterator.map(Parser(_)).reduceLeft(_ | _) ^^ (x ⇒ variableMap(x))

  def sparse(expression: String) = this.parseAll(b_expression, expression)
}

object LogicalExpression {

  def sparse(variables: Map[String, Boolean])(value: String) {
    println(LogicalExpression(variables).sparse(value))
  }
}

object Sofoklis {

  def main(args: Array[String]) {
    println("testing parser")

    val variables = Map("a" -> true, "b" -> false, "c" -> true)
    val variableParser = LogicalExpression.sparse(variables) _

    variableParser("a or b")
    variableParser("a and b")
    variableParser("a or b or c and a")
    variableParser("a and b or ((a and b) or a)")
    variableParser("a or b and c or (a and c)or a")
    variableParser("a and b and a and c or b")
    variableParser("a or b or d")
    variableParser("a and b and c")
    variableParser("")

  }
}
