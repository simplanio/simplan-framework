package com.intuit.data.simplan.global.utils

import java.util.UUID

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 11-Nov-2021 at 10:54 AM
  */
object IdGenerator {
  def randomUUID: String = UUID.randomUUID().toString.replaceAll("-", "")
}
