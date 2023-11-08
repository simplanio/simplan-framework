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

package com.intuit.data.simplan.parser.sqlflow.client

import com.intuit.data.simplan.parser.sqlflow.client.SqlFlowClient.rootNodes
import com.intuit.data.simplan.parser.sqlflow.{Edge, Graph, Node, SqlFlowGraph}

class SqlFlowClient(sqlFlowGraph: SqlFlowGraph) extends Serializable {

  def columnPopularity(sql: String): Set[Edge] = {
    val graph = sqlFlowGraph.createGraph(sql)
    val nodes: Set[Node] = rootNodes(graph)
    val edgesFromRootNodes: Set[Edge] = nodes.map(node => {
      val edges: Set[Edge] = graph.edges.filter(_.source.name.equalsIgnoreCase(node.name) && node.name != "")
      edges
    }).flatten

    edgesFromRootNodes
  }

}

object SqlFlowClient extends Serializable {

  def apply(dialect: String): SqlFlowClient = {
    val sqlFlowGraph = SqlFlowGraph(dialect)
    new SqlFlowClient(sqlFlowGraph)
  }

  /** No incoming edges towrads the node
    * @param graph
    * @return
    */
  def rootNodes(graph: Graph): Set[Node] = {
    val fromNodes = graph.edges.map(_.source)
    val toNodes = graph.edges.map(_.target)
    val rootNodes = fromNodes.diff(toNodes)
    rootNodes
  }
}
