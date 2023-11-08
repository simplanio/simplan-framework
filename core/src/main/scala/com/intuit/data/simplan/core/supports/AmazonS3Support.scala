package com.intuit.data.simplan.core.supports

import com.intuit.data.simplan.common.files.FileUtils
import com.intuit.data.simplan.core.aws.AmazonS3FileUtils
import com.intuit.data.simplan.core.context.Support

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 25-Mar-2022 at 9:37 AM
  */
trait AmazonS3Support extends Support {
  override lazy val fileUtils: FileUtils = AmazonS3FileUtils(appContextConfig)
}
