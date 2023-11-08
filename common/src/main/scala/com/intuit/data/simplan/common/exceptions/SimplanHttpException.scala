package com.intuit.data.simplan.common.exceptions

import com.intuit.data.simplan.global.exceptions.SimplanException

/**
  * @author - Abraham, Thomas - tabaraham1
  *         Created on 8/19/21 at 12:51 AM
  */
class SimplanHttpException(val code: Int, val endpoint: String, private val cause: Throwable = None.orNull) extends SimplanException(s"Http Request failed to ${endpoint} with code : $code", cause) with Serializable
