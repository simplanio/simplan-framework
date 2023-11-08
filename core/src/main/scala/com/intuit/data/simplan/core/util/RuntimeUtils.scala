package com.intuit.data.simplan.core.util

import scala.util.Try

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 17-Mar-2022 at 11:52 PM
  */
object RuntimeUtils {
  def isDatabricksRuntime: Boolean = Try(Class.forName("com.databricks.DatabricksMain")).isSuccess
}
