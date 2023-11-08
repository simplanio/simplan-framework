package com.intuit.data.simplan.core.aws

import com.intuit.data.simplan.global.domain.QualifiedParam

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 25-Mar-2022 at 11:32 AM
  */
case class AwsSystemConfig(sts: Option[AwsSTSSystemConfig])

case class AwsSTSSystemConfig(arn: QualifiedParam, sessionPrefix: Option[String])
