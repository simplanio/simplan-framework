package com.intuit.data.simplan.logging

import org.slf4j.{Logger, LoggerFactory}

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 29-Nov-2021 at 9:31 AM
  */
trait Logging extends Serializable {
  @transient protected lazy val logger: Logger = LoggerFactory.getLogger(getClass)
}
