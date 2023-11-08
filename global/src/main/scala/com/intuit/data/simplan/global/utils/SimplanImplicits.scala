package com.intuit.data.simplan.global.utils

import com.intuit.data.simplan.global.json.SimplanJsonMapper
import org.slf4j.{Logger, LoggerFactory}

import java.util.Properties
import scala.reflect.ClassTag
import scala.util.Try

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 03-Dec-2021 at 12:09 PM
  */
object SimplanImplicits extends Serializable {
  @transient private lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  implicit class Pipe[T](val v: T) extends AnyRef {
    def |[U](f: T => U): U = f(v)
    def pipe[U](f: T => U): U = f(v)
  }

  implicit class TimerImplicits[T](f: => T) extends Serializable {
    def timeIt(message: String, loggerLevel: String => Unit = logger.debug): T = ExecutionUtils.timeItLog(loggerLevel, message)(f)
    def timeIt(message: String): T = ExecutionUtils.timeItLog(message)(f)
  }

  implicit class ProductImplicits(c: Product) extends Serializable {

    /**
      * Uses Reflection. This is not Typesafe. So use with Caution!
      */
    def fromNameSafely[T](name: String): Option[T] = Try { fromName[T](name) }.toOption
    def fromName[T](name: String): T = c.productElement(c.getClass.getDeclaredFields.map(_.getName).indexOf(name)).asInstanceOf[T]
  }

  implicit class ToJsonImplicits(c: AnyRef) extends Serializable {
    def toJsonSafely: Option[String] = Try(toJson).toOption
    def toJson: String = SimplanJsonMapper.toJson(c)
  }

  implicit class FromJsonImplicits(data: String) extends Serializable {
    def fromJsonSafely[T <: AnyRef](implicit m: ClassTag[T]): Option[T] = Try(fromJson).toOption
    def fromJson[T <: AnyRef](implicit m: ClassTag[T]): T = SimplanJsonMapper.fromJson(data)
  }

  implicit class MapStringImplicits(data: Map[String, String]) extends Serializable {

    def toProperties: Properties = {
      val producerProperties = new Properties()
      data.foreach(each => producerProperties.put(each._1, each._2))
      producerProperties
    }
  }
}
