package com.intuit.data.simplan.core.domain.operator

/**
  * @author - Abraham, Thomas - tabaraham1
  *         Created on 8/19/21 at 10:59 PM
  */
class OperatorResponse(val canContinue: Boolean, val responseValues: Map[String, java.io.Serializable] = Map.empty, val throwable: Option[Throwable] = None, val message: String = "") extends Serializable

object OperatorResponse {
  def continue = new OperatorResponse(canContinue = true)
  def continue(responseValues: (String, java.io.Serializable)*) = new OperatorResponse(canContinue = true, responseValues = responseValues.toMap)

  def continue(responseValues: Map[String, java.io.Serializable]) = new OperatorResponse(canContinue = true, responseValues = responseValues)
  def dontContinue = new OperatorResponse(canContinue = false)
  def shouldContinue(continue: Boolean) = new OperatorResponse(continue)
}
