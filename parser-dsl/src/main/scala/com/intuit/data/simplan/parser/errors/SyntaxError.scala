package com.intuit.data.simplan.parser.errors

import org.antlr.v4.runtime.{RecognitionException, Recognizer}

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 09-Oct-2021 at 10:17 AM
  */
case class SyntaxError(
    private val recognizer: Recognizer[_, _],
    private val offendingSymbol: Any,
    private val line: Int,
    private val charPositionInLine: Int,
    private val message: String,
    private val e: RecognitionException
)
