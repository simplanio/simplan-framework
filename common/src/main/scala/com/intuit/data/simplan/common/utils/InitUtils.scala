package com.intuit.data.simplan.common.utils

import com.intuit.data.simplan.common.config.SimplanEmitterConfig
import com.intuit.data.simplan.common.emitters.SimplanEmitter
import com.intuit.data.simplan.common.exceptions.SimplanConfigException
import com.intuit.data.simplan.logging.Logging

import scala.reflect.ClassTag

/** @author Abraham, Thomas - tabraham1
  *         Created on 04-Aug-2022 at 3:24 PM
  */
object InitUtils extends Logging {

  //TODO: Thomas - Need to improve this instantiation. This could lead into problems in the future.
  // Need to Explore TypeTags/ClassTags to get the right tags to instantiate the right constructor types
  def instantiate[T](cls: Class[_], constructorArgs: AnyRef)(implicit ct: ClassTag[T]): T = {
    val operatorClass =
      if (ct.runtimeClass.isAssignableFrom(cls)) {
        cls.asInstanceOf[Class[_ <: T]]
      } else {
        throw new SimplanConfigException(s"${cls.getCanonicalName} doesnt extend ${ct.runtimeClass.getClass.getCanonicalName}.")
      }
    val constructorCls: Array[Class[_]] = operatorClass.getConstructors.head.getParameters.map(_.getParameterizedType.getTypeName).map(Class.forName)
    operatorClass.getConstructor(constructorCls.head).newInstance(constructorArgs)

  }

  def instantiate[T](cls: Class[_], constructorArgs: List[AnyRef])(implicit ct: ClassTag[T]): T = {
    val operatorClass =
      if (ct.runtimeClass.isAssignableFrom(cls)) {
        cls.asInstanceOf[Class[_ <: T]]
      } else {
        throw new SimplanConfigException(s"${cls.getCanonicalName} doesnt extend ${ct.runtimeClass.getClass.getCanonicalName}.")
      }
    val constructorCls: Array[Class[_]] = operatorClass.getConstructors.head.getParameters.map(_.getParameterizedType.getTypeName).map(Class.forName)
    val value = operatorClass.getConstructor(constructorCls.toArray: _*)
    value.newInstance(constructorArgs: _*)

  }

  def instantiateEmitter(key: String, emitterConfig: SimplanEmitterConfig): SimplanEmitter = instantiate[SimplanEmitter](Class.forName(emitterConfig.handler), emitterConfig)
}
