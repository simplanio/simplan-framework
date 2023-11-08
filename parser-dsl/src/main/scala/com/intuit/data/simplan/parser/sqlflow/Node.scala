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

package com.intuit.data.simplan.parser.sqlflow

import com.intuit.data.simplan.parser.sql.Expression

trait Node extends Serializable {
  def name:String
}
trait PrimaryRelationNode extends Node

case class TableNode(name:String, schema:Option[String], catalog:Option[String]) extends Node
case class ColumnNode(name:String, node:Option[Node]) extends Node
case class ExpressionNode(name:String="",expression:Expression) extends Node
case class AliasNode(name:String="",alias:String, columnNodes:Option[Set[ColumnNode]]) extends Node

case class Edge(source:Node, target:Node, attributes:Map[String,Any]=Map.empty)

