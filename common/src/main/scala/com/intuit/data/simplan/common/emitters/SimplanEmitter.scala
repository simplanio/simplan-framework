package com.intuit.data.simplan.common.emitters

import com.intuit.data.simplan.common.config.SimplanEmitterConfig
import com.intuit.data.simplan.global.json.SimplanJsonMapper
import com.intuit.data.simplan.global.utils.SimplanImplicits.ToJsonImplicits

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 04-Aug-2022 at 9:09 AM
  */
abstract class SimplanEmitter(emitterConfig: SimplanEmitterConfig) extends Serializable {
  protected def emitInternal(message: String, target: Option[String] = None, key: Option[String] = None): Boolean

  def emit(message: String): Boolean = emitInternal(message, None, None)
  def emit(target: String, message: String): Boolean = emitInternal(message, Option(target), None)
  def emit(target: String, key: String, message: String): Boolean = emitInternal(message, Option(target), Option(key))

  def emitObject(message: AnyRef): Boolean = emitInternal(message.toJson, None, None)
  def emitObjectWithKey(message: AnyRef,key:String): Boolean = emitInternal(message.toJson, None, Option(key))
  def emitObject(target: String, message: AnyRef): Boolean = emitInternal(message.toJson, Option(target), None)
  def emitObject(target: String, key: String, message: AnyRef): Boolean = emitInternal(message.toJson, Option(target), Option(key))

  protected def parseConfigAs[T](implicit m: Manifest[T]): T = SimplanJsonMapper.fromJson[T](emitterConfig.config)
}
