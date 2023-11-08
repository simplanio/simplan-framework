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

import com.intuit.data.simplan.parser.sql.catalog.{MetastoreCatalogProvider, ThriftMetastoreCatalogProvider}
import com.intuit.data.simplan.parser.sqlflow.{AliasNode, ColumnNode, Edge, ExpressionNode, Graph, Node, SqlFlowGraph, TableNode}

case class ColumnLineage(inputColumns:Set[ColumnNode], expressions:Set[ExpressionNode], output:ColumnNode)

// select statements do not have output table
case class Lineage(inputTables:Set[TableNode] , output:Option[TableNode], columnLineage:Set[ColumnLineage])

class LineageClient(sqlFlowGraph:SqlFlowGraph){

  /**
    * Returns Lineage which consists of input,output tables and column Lineage
    * @param sql
    * @return
    */
  def lineage(sql:String): Lineage ={

    val graph:Graph  = sqlFlowGraph.createGraph(sql)
    val nodes:Set[Node] = graph.nodes
    val edges:Set[Edge] = graph.edges

    val adjaencyList:(Map[Node,Set[Node]], Map[Node,Set[Node]]) = createAjaencyList(edges)

    val outgoingEdgesMap:Map[Node,Set[Node]] = adjaencyList._1
    val incomingEdgesMap:Map[Node,Set[Node]] = adjaencyList._2

    // a graph can contain multiple subgraphs
    // output graph incoming nodes
    val outputTable:Option[TableNode] = if (!incomingEdgesMap.keySet.filter(node=>node.isInstanceOf[TableNode]).isEmpty){
      Some(incomingEdgesMap.keySet.filter(node=>node.isInstanceOf[TableNode]).map(_.asInstanceOf[TableNode]).head)
    }else None
    val aliasNodes = incomingEdgesMap.keySet.filter(node=>node.isInstanceOf[AliasNode])

    val outputColumns:Set[ColumnNode] = getOutputColumnNodes(incomingEdgesMap, outgoingEdgesMap, outputTable)
    val outputGraph: Set[ColumnLineage] = deriveColumnLineage(incomingEdgesMap, outputColumns)

    val inputTables = outgoingEdgesMap.keySet.filter(node => node.isInstanceOf[TableNode]).map(_.asInstanceOf[TableNode])

    val inputGraph:Map[Node,Set[ColumnLineage]] = if (!aliasNodes.isEmpty){
      aliasNodes.foldLeft(Map.empty[Node,Set[ColumnLineage]])((map, aliasNode)=>{
        val columns:Set[ColumnNode] = getOutputColumnNodes(incomingEdgesMap, outgoingEdgesMap, Some(aliasNode))
        val columnLineage:Set[ColumnLineage] = deriveColumnLineage(incomingEdgesMap, columns)
        map ++ Map(aliasNode -> columnLineage)
      })
    } else Map.empty

    val resolvedColumnLineage:Set[ColumnLineage] = resolve(outputGraph, inputGraph)

    Lineage(inputTables, outputTable, resolvedColumnLineage)

  }

  def createAjaencyList(edges:Set[Edge]): (Map[Node,Set[Node]], Map[Node,Set[Node]])  ={
    val outgoingEdgesMap:Map[Node,Set[Node]] =  edges.foldLeft(Map.empty[Node,Set[Node]])((map,edge)=>{
      val targetNodes:Set[Node] = if(map.get(edge.source).isDefined){
        val adjNodes:Set[Node] = map.get(edge.source).get
        adjNodes ++ Set(edge.target)
      } else Set(edge.target)
      map ++ Map(edge.source -> targetNodes)
    })

    val inComingEdgesMap:Map[Node,Set[Node]] =  edges.foldLeft(Map.empty[Node,Set[Node]])((map,edge)=>{
      val sourceNodes:Set[Node] = if(map.get(edge.target).isDefined){
        val adjNodes = map.get(edge.target).get
        adjNodes ++ Set(edge.source)
      } else Set(edge.source)
      map ++ Map(edge.target -> sourceNodes)
    })

    (outgoingEdgesMap, inComingEdgesMap)
  }


  /**
    * gets the right most(output) column nodes in a graph
    * @param inComingEdgesMap
    * @param outgoingEdgesMap
    * @param outputNode
    * @return
    */
  def getOutputColumnNodes(inComingEdgesMap:Map[Node,Set[Node]], outgoingEdgesMap:Map[Node,Set[Node]] , outputNode:Option[Node]):Set[ColumnNode]= {
    if(outputNode.isDefined){
      val outputColumnsOpt = inComingEdgesMap.get(outputNode.get)
      if (outputColumnsOpt.isDefined) {
        // target Columns
        outputColumnsOpt.get.filter(node => node.isInstanceOf[ColumnNode]).map(_.asInstanceOf[ColumnNode])
      }else Set.empty[ColumnNode]
    }else {
      val inComingNodes = inComingEdgesMap.keySet
      val outgoingNodes = outgoingEdgesMap.keySet
      val leafNodes = inComingNodes.diff(outgoingNodes)
      val outputColumnNodes = leafNodes.filter(node=>node.isInstanceOf[ColumnNode]).map(_.asInstanceOf[ColumnNode])
      outputColumnNodes
    }

  }

  /**
    * Derives column Lineage for a given graph
    * @param inComingEdgesMap
    * @param outputColumns
    * @return
    */
  def deriveColumnLineage(inComingEdgesMap:Map[Node,Set[Node]], outputColumns:Set[ColumnNode]):Set[ColumnLineage]= {
    val columnLineages:Set[ColumnLineage] = outputColumns.foldLeft(Set.empty[ColumnLineage])((columnLineages, outputColumn)=>{
      val nodesOpt = inComingEdgesMap.get(outputColumn)
      val (columns,expressionNodes):(Set[ColumnNode],Set[ExpressionNode]) = if (nodesOpt.isDefined){
        val expressionNodes:Set[ExpressionNode] = nodesOpt.get.filter(_.isInstanceOf[ExpressionNode]).map(_.asInstanceOf[ExpressionNode])
        val columns:Set[ColumnNode] =  expressionNodes.foldLeft(Set.empty[ColumnNode])((sourceColumns,expNode)=>{
          val columns:Set[ColumnNode] = if (inComingEdgesMap.get(expNode).isDefined){
            inComingEdgesMap.get(expNode).get.filter(_.isInstanceOf[ColumnNode]).map(_.asInstanceOf[ColumnNode])
          }else Set.empty[ColumnNode]
          sourceColumns ++ columns
        })
        (columns, expressionNodes)
      } else (Set.empty[ColumnNode], Set.empty[ExpressionNode])
      val columnLineage:ColumnLineage = ColumnLineage(columns, expressionNodes, outputColumn)
      columnLineages + columnLineage
    })
    columnLineages
  }

  /**
    * Creates a flatten structure across multiple subgraphs
    * @param outputGraph
    * @param inputGraph
    */
  def resolve(outputGraphs:Set[ColumnLineage], inputGraph:Map[Node,Set[ColumnLineage]]): Set[ColumnLineage] ={

    outputGraphs.map(outputGraph=>{
      val outputSourceColumns:Set[ColumnNode] = outputGraph.inputColumns

      val sourceColumnsExpressions:Set[(Set[ColumnNode], Set[ExpressionNode])] = outputSourceColumns.map(outputSourceColumn=>{
        val sourceNodeOpt:Option[Node] = outputSourceColumn.node
        if (sourceNodeOpt.isDefined){
          val sourceNode:Node = sourceNodeOpt.get
          val connectingSubGraph = inputGraph.get(sourceNode)
          if (connectingSubGraph.isDefined){
            val inputLineage:Set[ColumnLineage] = connectingSubGraph.get
            // always 1 output Column
            val columnLineage = inputLineage.filter(col=>{
              col.output.name.equalsIgnoreCase(outputSourceColumn.name)
            }).head

            val sourceColumns:Set[ColumnNode] = columnLineage.inputColumns
            val expressions:Set[ExpressionNode] = columnLineage.expressions
            (sourceColumns, expressions)
          } else (Set.empty[ColumnNode], Set.empty[ExpressionNode])
        } else (Set.empty[ColumnNode], Set.empty[ExpressionNode])
      })

      val sourceColumns:Set[ColumnNode] = sourceColumnsExpressions.map(_._1).flatten
      val expressions:Set[ExpressionNode] = sourceColumnsExpressions.map(_._2).flatten

      if (!sourceColumns.isEmpty){
        val newExpressions = expressions ++ outputGraph.expressions
        val columnLineage = ColumnLineage(sourceColumns, newExpressions, outputGraph.output)
        val finalColumnLineage:Set[ColumnLineage] = resolve(Set(columnLineage), inputGraph)
        finalColumnLineage
      } else {
        Set(outputGraph)
      }
    }).flatten

  }

}

object LineageClient {

  def apply(): LineageClient ={
    val sqlFlowGraph = SqlFlowGraph()
    new LineageClient(sqlFlowGraph)
  }



}
