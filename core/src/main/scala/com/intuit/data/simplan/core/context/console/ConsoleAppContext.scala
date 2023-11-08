package com.intuit.data.simplan.core.context.console

import com.intuit.data.simplan.core.context.{AppContext, InitContext}

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 05-Oct-2021 at 11:51 AM
  */
class ConsoleAppContext(val initContext: InitContext) extends AppContext(initContext) {
  def this(configs: Array[String]) = this(InitContext(configs))
}
