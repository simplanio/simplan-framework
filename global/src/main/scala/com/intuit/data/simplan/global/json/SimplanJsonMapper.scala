package com.intuit.data.simplan.global.json

import com.fasterxml.jackson.databind.{Module, ObjectMapper}

import scala.reflect.ClassTag
import scala.util.Try

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 14-Feb-2022 at 9:48 AM
  */
abstract class SimplanJsonMapper {
  def toJson(obj: AnyRef): String
  def fromJson[T](json: String, clazz: Class[T]): T
  def fromJson[T](json: String)(implicit m: ClassTag[T]): T = fromJson(json, m.runtimeClass.asInstanceOf[Class[T]])

  def tryToJson(obj: AnyRef): Try[String] = Try(toJson(obj))
  def tryFromJson[T](json: String, clazz: Class[T]): Try[T] = Try(fromJson(json, clazz))
  def tryFromJson[T](json: String)(implicit m: ClassTag[T]): Try[T] = tryFromJson(json, m.runtimeClass.asInstanceOf[Class[T]])
}

object SimplanJsonMapper extends SimplanJsonMapper {
  private val mapper = new JacksonJsonMapper()
  override def toJson(obj: AnyRef): String = mapper.toJson(obj)
  override def fromJson[T](json: String, clazz: Class[T]): T = mapper.fromJson(json, clazz)
  def registerModule(module: Module): ObjectMapper = mapper.objectMapper.registerModule(module)
}
