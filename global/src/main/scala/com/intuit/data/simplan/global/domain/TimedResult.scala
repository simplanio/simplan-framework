package com.intuit.data.simplan.global.domain

import org.apache.commons.lang.time.DurationFormatUtils

import java.time.Duration

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 14-Dec-2021 at 4:05 PM
  */
case class TimedResult[T](result: T, duration: Duration) {
  def inMillis: Long = duration.toMillis
  def inWords: String = DurationFormatUtils.formatDurationWords(duration.toMillis, true, false)
  def inHMS: String = DurationFormatUtils.formatDurationHMS(duration.toMillis)
  override def toString: String = inWords
}
