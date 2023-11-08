package com.intuit.data.simplan.parser.sql

import com.intuit.data.simplan.parser.utils.RegexUtils.RichRegex
import org.apache.commons.lang.StringUtils

import scala.util.matching.Regex
import scala.util.{Random, Try}

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 03-Mar-2022 at 3:26 PM
  */

object SqlClassifier {
  val SPACE = " "
  val DOT = "\\."
  val UNDERSCORE = "_"

  val sqlRegexMap = Map(
    QueryType.CTAS -> "(?i)^\\s*create\\s+(?:temporary\\s+)?table\\s+(?:if\\s+not\\s+exists\\s+)?([\\$0-9a-zA-Z-\\:\\{0-9a-zA-Z-\\}\\._]+)\\s+as[\\s\\n]+([\\s\\S]*)".r,
    QueryType.EXT -> "(?i)^\\s*create\\s+external\\s+table\\s+(?:if\\s+not\\s+exists\\s+)?([\\$0-9a-zA-Z-\\:\\{0-9a-zA-Z-\\}\\._]+)\\s+([\\s\\S]*)".r,
    QueryType.DROP -> "(?i)^\\s*drop\\s+table\\s+(?:if\\s+exists\\s+)?([\\$0-9a-zA-Z-\\:\\{0-9a-zA-Z-\\}\\._]+)".r,
    QueryType.TRUNCATE -> "(?i)^\\s*truncate\\s+table\\s+(?:if\\s+exists\\s+)?([\\$0-9a-zA-Z-\\:\\{0-9a-zA-Z-\\}\\._]+)".r,
    QueryType.INSERT -> "(?i)^\\s*insert\\s+into\\s+(?:table\\s+)?([\\$0-9a-zA-Z-\\:\\{0-9a-zA-Z-\\}\\._]+)\\s+([\\s\\S]*)".r,
    QueryType.INSERT_OVERWRITE -> "(?i)^\\s*insert\\s+overwrite\\s+table\\s+([\\$0-9a-zA-Z-\\:\\{0-9a-zA-Z-\\}\\._]+)\\s+([\\s\\S]*)".r,
    QueryType.USE -> "(?i)^\\s*use\\s+([\\$0-9a-zA-Z-\\:\\{0-9a-zA-Z-\\}\\._]+).*".r,
    QueryType.SELECT -> "(?im)^select.*from\\s(([\\w_]+\\.)?[\\w_]+)".r
  )

  def classify(query: String, index: Int = Random.nextInt(10000)): (String, QueryType) = {
    sqlRegexMap.foreach {
      case (queryType, pattern: Regex) =>
        if (pattern matches query) {
          val extractedMatch = Try(pattern findAllIn query group 1)
            .getOrElse("UNKNOWN")
            .replaceAll(DOT, UNDERSCORE)
            .replaceAll(SPACE, UNDERSCORE)
            .replaceAll("\\$", StringUtils.EMPTY)
            .replaceAll("\\{", StringUtils.EMPTY)
            .replaceAll("}", StringUtils.EMPTY)
            .replaceAll(":", "-")
          return (s"$index-$queryType-${extractedMatch.toUpperCase()}", queryType)
        }
    }
    (s"$index-UNKNOWN", QueryType.UNKNOWN)
  }

  def main(args: Array[String]): Unit = {
    println(classify("create table abc.xyz as select * from abc", 1))
    println(classify("drop table something", 2))
    println(classify("create external table abz.xyz as select ", 2))
    println(classify("insert into abc as select * from something", 3))
    println(classify("insert overwrite table abc as select * from something;", 4))
    println(classify("use database", 5))
    println(classify("select abc from someTable", 5))
    println(classify("somthing it cannot understand", 6))
  }

}
