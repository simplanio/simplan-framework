package com.intuit.data.simplan.logging.utils

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, JsonSerializer, SerializerProvider}

import java.time.Instant

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 07-Jan-2022 at 3:37 PM
  */
class InstantDeSerializer extends JsonDeserializer[Instant] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): Instant = Instant.parse(p.getText)
}

class InstantSerializer extends JsonSerializer[Instant] {
  override def serialize(value: Instant, gen: JsonGenerator, serializers: SerializerProvider): Unit = gen.writeString(MetricUtils.toISODateFormat(value))
}
