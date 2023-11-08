package com.intuit.data.simplan.core.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, JsonNode}
import com.intuit.data.simplan.core.domain.WindowType
import com.intuit.data.simplan.core.domain.operator.config.transformations.{SessionWindowOperatorConfig, SlidingWindowOperatorConfig, TumbleWindowOperatorConfig, WindowOperatorConfig}

class WindowOperatorJacksonDesrializer extends JsonDeserializer[WindowOperatorConfig] with Serializable {

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): WindowOperatorConfig = {
    val objectMapper: JsonMapper = p.getCodec.asInstanceOf[JsonMapper]
    val node: JsonNode = objectMapper.readTree(p)
    val windowTypeOpr: WindowType = WindowType.valueOf(node.get("windowType").asText())
    val windowType = node.get("windowType").asText()
    val timeColumn = node.get("timeColumn").asText()
    val windowDuration = node.get("windowDuration").asText()
    windowTypeOpr match {
      case WindowType.SLIDING => {
        val slideDuration = node.get("slideDuration").asText()
        SlidingWindowOperatorConfig(windowType, timeColumn, windowDuration, slideDuration)
      }
      case WindowType.TUMBLING => TumbleWindowOperatorConfig(windowType, timeColumn, windowDuration)
      case WindowType.SESSION =>
        val slideDuration = node.get("slideDuration").asText()
        val startTime = node.get("startTime").asText()
        SessionWindowOperatorConfig(windowType, timeColumn, windowDuration, slideDuration, startTime)
      case _ => throw new Exception("invalid widntow type")
    }
  }
}
