package com.intuit.data.simplan.global.qualifiedstring

import com.intuit.data.simplan.global.domain.QualifiedParam
import com.intuit.data.simplan.global.exceptions.QualifiedParamResolutionException

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 07-Jan-2022 at 1:54 PM
  */
object QualifiedParameterManager {
  private val qualifiedParamHandlers = new scala.collection.mutable.HashMap[String, QualifiedParamHandler[_]]()

  registerHandler(new RawQualifiedString)

  def registerHandler(handler: QualifiedParamHandler[_]): Unit = qualifiedParamHandlers ++= List(handler.qualifier.toUpperCase -> handler)

  def resolve[T](qualifiedParam: QualifiedParam): T = {
    val upperCaseQualifier = qualifiedParam.qualifier.toUpperCase
    val resolved = (qualifiedParamHandlers.get(upperCaseQualifier), qualifiedParam.referenceQs) match {
      case (Some(matchedQualifier), Some(referenceQs)) => matchedQualifier.resolve(new QualifiedParam(resolve[String](referenceQs)))
      case (Some(matchedQualifier), None)              => matchedQualifier.resolve(qualifiedParam)
      case (None, None)                                => throw new QualifiedParamResolutionException(qualifiedParam,qualifiedParamHandlers.keySet.toList)
      case (None, Some(_))                             => throw new QualifiedParamResolutionException(qualifiedParam,qualifiedParamHandlers.keySet.toList)
    }
    resolved.asInstanceOf[T]
  }
}
