package com.intuit.data.simplan.core.util

import com.intuit.data.simplan.core.domain.ScriptCommandLineParameters

import scala.util.Try

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 07-Jun-2022 at 1:24 PM
  */
object ScriptUtils {

  def getEnvironment(environmentOption: Option[String]) = {
    val environmentFallback = Try(System.getenv("env")).getOrElse(null)
    val systemPropertiesFallback = Option(Try(System.getProperty("env")).getOrElse(null)).getOrElse("prd")
    val _environment = Option(environmentOption.getOrElse(environmentFallback)).getOrElse(systemPropertiesFallback)
    val environment = _environment.toUpperCase match {
      case "DEV" | "BETA" => "beta"
      case "PROD" | "PRD" => "prd"
      case _              => _environment.toLowerCase
    }
    environment
  }
}
