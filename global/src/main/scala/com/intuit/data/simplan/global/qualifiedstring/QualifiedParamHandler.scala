package com.intuit.data.simplan.global.qualifiedstring

import com.intuit.data.simplan.global.domain.QualifiedParam

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 07-Jan-2022 at 1:48 PM
  */
trait QualifiedParamHandler[T] extends Serializable {
  val qualifier: String
  def resolve(qualifiedString: QualifiedParam): T
}
