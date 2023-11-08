package com.intuit.data.simplan.core.operators.io

import com.intuit.data.simplan.core.context.AppContext
import com.intuit.data.simplan.core.domain.operator._
import com.intuit.data.simplan.global.utils.DateTimeUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.slf4j.{Logger, LoggerFactory}

import scala.language.postfixOps

/**
 * @author Kiran Hiremath
 */
case class BackupOperatorConfig(sourceDir: String,
                                destinationDir: String, versioned: Boolean = true) extends OperatorConfig

class BackupOperator(appContext: AppContext, operatorContext: OperatorContext) extends BaseOperator[BackupOperatorConfig](appContext, operatorContext) {
  @transient lazy val logger: Logger = LoggerFactory.getLogger(classOf[BackupOperator])

  override def process(request: OperatorRequest): OperatorResponse = {
    val version = FastDateFormat.getInstance("yyyyMMddHHmmss")
    val versionToday = version.format(DateTimeUtils.epochMilli)
    val destPath = if (operatorConfig.destinationDir.endsWith("/"))
      operatorConfig.destinationDir
    else operatorConfig.destinationDir + "/"

    val destinationKey = if (operatorConfig.versioned) {
      destPath + versionToday + "/"
    } else destPath
    if(appContext.fileUtils.copy(operatorConfig.sourceDir, destinationKey))
      logger.info("Backup Succeeded!!")
    else logger.error("Backup Failed!!")

    OperatorResponse.continue
  }

}
