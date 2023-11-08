package com.intuit.data.simplan.global.exceptions

/**
  * @author - Abraham, Thomas - tabaraham1
  *         Created on 8/19/21 at 12:48 AM
  */
class SimplanException(private val message: String = "", private val cause: Throwable = None.orNull) extends Exception(message, cause) with Serializable {

  def this(message: String) = this(message, None.orNull)

}
