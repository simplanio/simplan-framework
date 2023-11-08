package com.intuit.data.simplan.common.emitters

import com.intuit.data.simplan.common.config.{SimplanAppContextConfiguration, SimplanEmitterConfig}
import com.intuit.data.simplan.common.utils.InitUtils

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 04-Aug-2022 at 10:38 AM
  */
class SimplanEmitterRegistry(config: SimplanAppContextConfiguration) {

  private val emitters: Map[String, SimplanEmitter] = config.emitters.map {
    case (key: String, emitter: SimplanEmitterConfig) =>
      if (emitter.enabled.isDefined && emitter.enabled.get)
        (key, InitUtils.instantiateEmitter(key, emitter))
      else (key, new SimplanDummyEmitter(key))
  }

  def get(key: String): SimplanEmitter = emitters.getOrElse(key, new SimplanDummyEmitter(s"UndefinedKey:$key"))

}

object SimplanEmitterRegistry {
  def apply(config: SimplanAppContextConfiguration): SimplanEmitterRegistry = new SimplanEmitterRegistry(config)
}
