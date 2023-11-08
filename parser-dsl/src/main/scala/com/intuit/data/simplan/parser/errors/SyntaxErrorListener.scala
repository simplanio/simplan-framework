package com.intuit.data.simplan.parser.errors

import com.intuit.data.simplan.parser.errors
import org.antlr.v4.runtime.{BaseErrorListener, RecognitionException, Recognizer}

import scala.collection.mutable.ListBuffer

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 09-Oct-2021 at 10:19 AM
  */
class SyntaxErrorListener extends BaseErrorListener {

  private val _syntaxErrors = new ListBuffer[SyntaxError]

  def syntaxErrors: List[SyntaxError] = _syntaxErrors.toList

  override def syntaxError(recognizer: Recognizer[_, _], offendingSymbol: Any, line: Int, charPositionInLine: Int, msg: String, e: RecognitionException): Unit =
    _syntaxErrors += errors.SyntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e)

}
