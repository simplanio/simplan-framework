package com.intuit.data.simplan.logging.events

import com.intuit.data.simplan.logging.domain.v2.SimplanOpsEvent
import com.intuit.data.simplan.logging.utils.JacksonJsonMapper
import org.apache.commons.lang.StringUtils

import java.time.Instant
import scala.collection.JavaConverters._

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 12-Apr-2022 at 4:13 PM
  */
trait SimplanEvent extends Serializable {
  val `@timestamp`: Instant = Instant.now()
  val message: String = StringUtils.EMPTY
  val detailedMessage: String = StringUtils.EMPTY
  val tags: Array[String] = Array.empty
  val labels: Map[String, String] = Map.empty

  def toOpsEvent: SimplanOpsEvent = {
    new SimplanOpsEvent()
      .setTimestamp(`@timestamp`)
      .setMessage(message)
      .setDetailedMessage(detailedMessage)
      .setTags(tags.toList.asJava)
      .setLabels(labels.asJava)
  }
  def render: String = JacksonJsonMapper.toJson(toOpsEvent)

}
