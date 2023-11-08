package com.intuit.data.simplan.global.qualifiedstring

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer}
import com.intuit.data.simplan.global.domain.QualifiedParam

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 07-Jan-2022 at 3:37 PM
  */
class QualifiedParamJacksonDeserializer extends JsonDeserializer[QualifiedParam] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): QualifiedParam = new QualifiedParam(p.getText)
}
