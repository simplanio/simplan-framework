/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intuit.data.simplan.common.scripting.js

import com.intuit.data.simplan.common.exceptions.SimplanScriptingException
import com.intuit.data.simplan.logging.Logging

import javax.script._

/** @author Abraham, Thomas - tabraham1
  *         Created on 14-Feb-2023 at 3:34 PM
  */

case class JSFunctions(functionName: String, argList: List[String], functionBody: String) {
  val functionSignature = s"""${functionName}(${argList.mkString(",")})"""

  val functionDefinition: String =
    s"""function $functionSignature{
       |  $functionBody
       |}""".stripMargin
}

class SimplanJavaScriptScripting(val jsFunctions: List[JSFunctions]) extends Serializable with Logging {
  @transient lazy val engine: ScriptEngine = getScriptEngine
  @transient lazy val invokableEngine: Invocable = engine.asInstanceOf[Invocable]

  private def getScriptEngine: ScriptEngine = {
    val localEngine = new ScriptEngineManager().getEngineByName("nashorn")
    jsFunctions.foreach(jsFunction => {
      val compiledScript: CompiledScript = localEngine.asInstanceOf[Compilable].compile(jsFunction.functionDefinition)
      compiledScript.eval()
      logger.info(s"Registered Javascript Function to Engine ${jsFunction.functionSignature}")
    })
    localEngine
  }

  def evaluateBooleanExpression(functionName: String, args: AnyRef*): Boolean = {
    val value = evaluateExpression(functionName, args: _*)
    try { value.toString.toBoolean }
    catch { case e: Exception => throw new SimplanScriptingException(e.getMessage + s" while trying to convert ${value} to Boolean in functtion : $functionName") }
  }

  def evaluateExpression(functionName: String, args: AnyRef*): AnyRef = {
    try invokableEngine.invokeFunction(functionName, args: _*)
    catch {
      case e: Exception => throw new SimplanScriptingException(e.getMessage + s" while trying to execute $functionName",e)
    }
  }
}
