package com.intuit.data.simplan.core.domain.operator

/**
  * @author - Abraham, Thomas - tabaraham1
  *         Created on 8/21/21 at 10:39 PM
  */
case class XComWrapper(operatorClass: Class[_ <: Operator], operatorResponse: OperatorResponse) {
  def operatorExtends(cls: Class[_]): Boolean = cls isAssignableFrom operatorClass
}
