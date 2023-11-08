package com.intuit.data.simplan.logging.utils

import org.apache.commons.lang3.time.FastDateFormat

import java.time.Instant
import java.util.TimeZone
import scala.util.Try

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 18-Feb-2022 at 12:03 AM
  */
object MetricUtils {
  private val ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS: FastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("UTC"))

  def toISODateFormat(timestamp: Long): String = ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(timestamp)
  def toISODateFormat(instant: Instant): String = Try(toISODateFormat(instant.toEpochMilli)).toOption.orNull
  def toISODateFormatNullable(timestamp: Option[Long]): String = Try(toISODateFormat(timestamp.get)).toOption.orNull

  def currentEpochMilli: Long = Instant.now.toEpochMilli
  def currentEpochSecond: Long = Instant.now.getEpochSecond

}
