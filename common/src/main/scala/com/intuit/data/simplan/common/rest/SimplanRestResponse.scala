package com.intuit.data.simplan.common.rest

import com.intuit.data.simplan.global.json.SimplanJsonMapper

import scala.reflect.ClassTag

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 29-Mar-2022 at 12:37 PM
  */
case class SimplanRestResponse[T <: AnyRef](
    responseHeaders: Map[String, String],
    code: Int,
    responseString: String
)(implicit ct: ClassTag[T]) {

  val responseEntity: Option[T] = if (classOf[String].isAssignableFrom(ct.runtimeClass)) {
    Some(responseString.asInstanceOf[T])
  } else
    SimplanJsonMapper.tryFromJson(responseString)(ct).toOption
}
