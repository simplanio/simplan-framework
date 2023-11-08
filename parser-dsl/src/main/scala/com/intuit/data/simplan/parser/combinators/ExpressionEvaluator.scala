package com.intuit.data.simplan.parser.combinators

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 21-Oct-2021 at 12:53 PM
  */
class ExpressionEvaluator extends Serializable {
  val baseCombinator = new BaseCombinatorParser

  def boolExpression(check: String): Boolean = {
    val value: baseCombinator.ParseResult[Boolean] = baseCombinator.parseAll(baseCombinator.booleanOperations, check)
    if (value.successful)
      value.get
    else
      throw new Exception(value.toString)
  }

  def arithmeticExpression(check: String): Double = {
    val value: baseCombinator.ParseResult[Double] = baseCombinator.parseAll(baseCombinator.expr, check)
    if (value.successful)
      value.get
    else
      throw new Exception(value.toString)
  }
}

object ExpressionEvaluator {
  def apply(): ExpressionEvaluator = new ExpressionEvaluator()
}
