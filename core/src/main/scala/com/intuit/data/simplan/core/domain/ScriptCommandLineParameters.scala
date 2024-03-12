package com.intuit.data.simplan.core.domain

import org.apache.commons.lang.StringUtils

/** @author Abraham, Thomas - tabraham1
  *         Created on 09-May-2022 at 9:54 PM
  */
class ScriptCommandLineParameters(
    val configs: Array[String] = Array.empty,
    val script: String = StringUtils.EMPTY,
    val appName: String = "SimplanSparkApp",
    val project: Option[String] = None,
    val parentAppName: Option[String] = None,
    val assetId: Option[String] = None,
    val opsOwner: Option[String] = None,
    val businessOwner: Option[String] = None,
    val runId: Option[String] = None,
    val environment: Option[String] = None,
    val orchestratorId: Option[String] = None,
    val orchestrator: Option[String] = None,
    val dmrPipelineEntityIrn: Option[String] = None,
    val dmrProcessorEntityIrn: Option[String] = None,
    val dmrJobEntityIrn: Option[String] = None
) extends Serializable
