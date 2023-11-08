package com.intuit.data.simplan.parser.combinators

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 21-Oct-2021 at 9:48 AM
  */
object ParserUtils {

  def numberComparisons(num1: AnyVal, comparator: String, num2: AnyVal): Boolean = {
    if (num1.isInstanceOf[Double] || num2.isInstanceOf[Double]) {
      comparator match {
        case "==" => num1.toString.toDouble == num2.toString.toDouble
        case "!=" => num1.toString.toDouble != num2.toString.toDouble
        case ">=" => num1.toString.toDouble >= num2.toString.toDouble
        case "<=" => num1.toString.toDouble <= num2.toString.toDouble
        case "<"  => num1.toString.toDouble < num2.toString.toDouble
        case ">"  => num1.toString.toDouble > num2.toString.toDouble
      }
    } else {
      comparator match {
        case "==" => num1.toString.toLong == num2.toString.toLong
        case "!=" => num1.toString.toLong != num2.toString.toLong
        case ">=" => num1.toString.toLong >= num2.toString.toLong
        case "<=" => num1.toString.toLong <= num2.toString.toLong
        case "<"  => num1.toString.toLong < num2.toString.toLong
        case ">"  => num1.toString.toLong > num2.toString.toLong
      }
    }
  }

  def numberArithmeticOperations(num1: AnyVal, operator: String, num2: AnyVal): AnyVal = {
    if (num1.isInstanceOf[Double] || num2.isInstanceOf[Double]) {
      operator match {
        case "+" => num1.toString.toDouble + num2.toString.toDouble
        case "-" => num1.toString.toDouble - num2.toString.toDouble
        case "/" => num1.toString.toDouble / num2.toString.toDouble
        case "*" => num1.toString.toDouble * num2.toString.toDouble
      }
    } else {
      operator match {
        case "+" => num1.toString.toLong + num2.toString.toLong
        case "-" => num1.toString.toLong - num2.toString.toLong
        case "/" => num1.toString.toLong / num2.toString.toLong
        case "*" => num1.toString.toLong * num2.toString.toLong
      }
    }
  }
}
