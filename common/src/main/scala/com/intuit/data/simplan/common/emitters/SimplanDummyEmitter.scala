package com.intuit.data.simplan.common.emitters

import com.intuit.data.simplan.common.config.SimplanEmitterConfig
import com.intuit.data.simplan.logging.Logging

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 04-Aug-2022 at 9:09 AM
  */
class SimplanDummyEmitter(emitterKey: String) extends SimplanEmitter(SimplanEmitterConfig(classOf[SimplanDummyEmitter].getCanonicalName)) with Logging {

  override protected def emitInternal(message: String, target: Option[String] = None, key: Option[String] = None): Boolean = {
    logger.warn(s"Dummy Emitter handling emit to $emitterKey - Message - $message")
    false
  }
}
