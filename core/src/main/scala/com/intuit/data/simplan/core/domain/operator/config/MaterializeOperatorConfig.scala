package com.intuit.data.simplan.core.domain.operator.config

import com.intuit.data.simplan.core.domain.operator.OperatorConfig

case class MaterializeOperatorConfig(streamDataFrame:String, batchDataFrame:String, snapshotPath:String) extends OperatorConfig