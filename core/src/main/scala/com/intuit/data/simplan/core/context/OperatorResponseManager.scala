package com.intuit.data.simplan.core.context

import com.intuit.data.simplan.core.domain.operator.XComWrapper

import scala.collection.mutable

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 28-Mar-2022 at 9:17 PM
  */
class OperatorResponseManager {
  private val _operatorResponses = new mutable.HashMap[String, XComWrapper]()
  def operatorResponses: Map[String, XComWrapper] = _operatorResponses.toMap

  def append(response: Map[String, XComWrapper]): Unit = _operatorResponses ++= response
  def append(response: (String, XComWrapper)): Unit = _operatorResponses += response

}

object OperatorResponseManager {
  lazy val default = new OperatorResponseManager()
}
