package com.intuit.data.simplan.global.exceptions

import com.intuit.data.simplan.global.domain.QualifiedParam

/**
  * @author - Abraham, Thomas - tabaraham1
  *         Created on 8/19/21 at 12:48 AM
  */
class QualifiedParamResolutionException(private val message: String = "", private val cause: Throwable = None.orNull) extends SimplanExecutionException(message, cause) with Serializable {

  def this(qualifiedString: QualifiedParam,allowed:List[String]) = this(s"Unable to resolve Qualified String with qualifier=${qualifiedString.qualifier}. Registed handlers are : ${allowed.mkString(",")}", None.orNull)

}
