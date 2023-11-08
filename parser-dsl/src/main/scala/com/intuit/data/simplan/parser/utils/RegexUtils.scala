package com.intuit.data.simplan.parser.utils

import scala.util.matching.Regex

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 16-Aug-2022 at 4:53 PM
  */
object RegexUtils {

  implicit class RichRegex(val underlying: Regex) extends AnyVal {
    def matches(s: String): Boolean = underlying.pattern.matcher(s).matches
  }
}
