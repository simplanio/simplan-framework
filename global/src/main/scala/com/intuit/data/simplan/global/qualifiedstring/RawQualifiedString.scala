package com.intuit.data.simplan.global.qualifiedstring

import com.intuit.data.simplan.global.domain.QualifiedParam

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 07-Jan-2022 at 1:55 PM
  */
class RawQualifiedString extends QualifiedStringHandler {
  override val qualifier: String = "raw"

  override def resolve(qualifiedString: QualifiedParam): String = qualifiedString.string
}
