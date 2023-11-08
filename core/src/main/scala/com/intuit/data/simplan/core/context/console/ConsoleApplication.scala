package com.intuit.data.simplan.core.context.console

import com.intuit.data.simplan.core.context.{Application, DefaultRunParameters}

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 06-Oct-2021 at 11:30 AM
  */
class ConsoleApplication(consoleAppContext: ConsoleAppContext) extends Application(consoleAppContext)

object ConsoleApplication {
  def apply(appContext: ConsoleAppContext): ConsoleApplication = new ConsoleApplication(appContext)
}
