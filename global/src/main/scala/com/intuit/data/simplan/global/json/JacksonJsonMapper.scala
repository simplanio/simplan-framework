package com.intuit.data.simplan.global.json

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.JsonGenerator.Feature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{DeserializationFeature, JsonNode, MapperFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}
import com.intuit.data.simplan.global.domain.QualifiedParam
import com.intuit.data.simplan.global.qualifiedstring.QualifiedParamJacksonDeserializer
import org.slf4j.{Logger, LoggerFactory}

import java.time.Instant

/** @author Abraham, Thomas - tabraham1
  *         Created on 14-Feb-2022 at 9:50 AM
  */
class JacksonJsonMapper extends SimplanJsonMapper {

  @transient private lazy val logger: Logger = LoggerFactory.getLogger(classOf[JacksonJsonMapper])

  private[json] val objectMapper = {

    val simplanModule: SimpleModule = new SimpleModule("SimplanModule")
      .addDeserializer(classOf[QualifiedParam], new QualifiedParamJacksonDeserializer)
      .addDeserializer(classOf[Instant], new InstantDeSerializer)
      .addSerializer(classOf[Instant], new InstantSerializer)
    val mapper = new ObjectMapper() with ClassTagExtensions
    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(simplanModule)
      .configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, true)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
      .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
      .setSerializationInclusion(Include.NON_NULL)
      .setSerializationInclusion(Include.NON_EMPTY)
      .setSerializationInclusion(Include.NON_ABSENT)
      .configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
    mapper
  }

  override def toJson(obj: AnyRef): String = objectMapper.writeValueAsString(obj)

  override def fromJson[T](json: String, clazz: Class[T]): T = objectMapper.readValue(json, clazz)

  def fromJsonAsTree(json: String): JsonNode = objectMapper.readTree(json)
}
