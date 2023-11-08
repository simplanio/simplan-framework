package com.intuit.data.simplan.global.utils

import com.intuit.data.simplan.global.domain.TimedResult
import org.slf4j.{Logger, LoggerFactory}

import java.time.{Duration, Instant}
import scala.annotation.tailrec
import scala.util.Try

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 14-Dec-2021 at 3:42 PM
  */
object ExecutionUtils {
  @transient private lazy val logger: Logger = LoggerFactory.getLogger(getClass)

  def timeIt[T](f: => T): TimedResult[T] = {
    val start = Instant.now
    val result: T = f
    TimedResult(result, Duration.between(start, Instant.now))
  }

  def timeItLog[T](title: String)(f: => T): T = timeItLog(logger.debug, title)(f)

  def timeItLog[T](loggerLevel: String => Unit, title: String)(f: => T): T = {
    val timedResult = timeIt(f)
    loggerLevel(s"Execution time for $title: ${timedResult.inHMS}")
    timedResult.result
  }

  @tailrec
  def retry[T](times: Int, delayInSeconds: Long = 5, message: String = "Operation failed", loggerLevel: String => Unit = logger.warn, totalTries: Option[Int] = None)(fn: => T): T = {
    try {
      fn
    } catch {
      case exception: Exception =>
        val totalNumberOfTries = if (totalTries.isDefined) totalTries.get else times
        if (times > 0) {
          val attempt = totalNumberOfTries - times + 1
          loggerLevel(s"Attempt($attempt of ${totalNumberOfTries}) - $message: Retrying after $delayInSeconds seconds : ${exception.getMessage}")
          Thread.sleep(delayInSeconds * 1000)
          retry(times - 1, delayInSeconds, message, loggerLevel, Some(totalNumberOfTries))(fn)
        } else {
          throw exception
        }
    }
  }

  def retrySafely[T](times: Int, delayInSeconds: Long = 5, message: String = "Operation failed", loggerLevel: String => Unit = logger.warn)(fn: => T): Try[T] = {
    Try(retry[T](times, delayInSeconds, message, loggerLevel)(fn))
  }
}
