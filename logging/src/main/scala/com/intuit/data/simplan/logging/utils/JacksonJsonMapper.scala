package com.intuit.data.simplan.logging.utils

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.JsonGenerator.Feature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{DeserializationFeature, SerializationFeature}
import org.slf4j.{Logger, LoggerFactory}

import java.time.Instant

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 14-Feb-2022 at 9:50 AM
  */
object JacksonJsonMapper {

  @transient private lazy val logger: Logger = LoggerFactory.getLogger(JacksonJsonMapper.getClass)

  private val objectMapper = {
    val objectMapper: JsonMapper = JsonMapper.builder().build()
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    objectMapper.configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
    objectMapper.setSerializationInclusion(Include.NON_NULL)
    objectMapper.setSerializationInclusion(Include.NON_EMPTY)
    objectMapper.setSerializationInclusion(Include.NON_ABSENT)
    val simplanModule = new SimpleModule("SimplanDeSerializers")
    simplanModule.addDeserializer(classOf[Instant], new InstantDeSerializer)
    simplanModule.addSerializer(classOf[Instant], new InstantSerializer)
    objectMapper.registerModule(simplanModule)
    objectMapper
  }

  def toJson(obj: AnyRef): String = objectMapper.writeValueAsString(obj)

  def fromJson[T](json: String, clazz: Class[T]): T = objectMapper.readValue(json, clazz)
}
