package com.intuit.data.simplan.global.utils

import org.apache.commons.lang3.time.FastDateFormat

import java.time.Instant
import java.util.TimeZone
import scala.util.Try

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 16-Nov-2021 at 10:53 PM
  */
object DateTimeUtils {

  def epochMilli: Long = Instant.now().toEpochMilli
  def epochSeconds: Long = Instant.now().getEpochSecond
  def toISODateFormat(timestamp: Long): String = ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(timestamp)
  def toISODateFormat(instant: Instant): String = Try(toISODateFormat(instant.toEpochMilli)).toOption.orNull

  private val ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS: FastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("UTC"))
}
