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

import com.intuit.data.simplan.parser.ast.ASTTreeBuilder
import com.intuit.data.simplan.parser.ast.sparksql.SparkSqlASTTreeBuilder
import com.intuit.data.simplan.parser.sql._
import com.intuit.data.simplan.parser.sqlflow.SqlFlowGraph.generateSqlFlow

/** Constructs a SQl flow graph
  */

class SqlFlowGraph(astTreeBuilder: ASTTreeBuilder) extends Serializable {

  def createGraph(sql: String): Graph = {
    val parsedStatement: Statement = astTreeBuilder.astTree(sql)
    val graph: Graph = generateSqlFlow(parsedStatement)
    graph
  }

}

object SqlFlowGraph extends Serializable {

  def apply(dialect: String = "sparksql"): SqlFlowGraph = {
    val asttree: ASTTreeBuilder = dialect match {
      case "sparksql" => SparkSqlASTTreeBuilder()
      case _          => throw new Exception("Unsupported dialect")
    }
    new SqlFlowGraph(asttree)
  }

  val aliasMapping = scala.collection.mutable.HashMap.empty[String, Node]

  def namedExpressionGraph(sourceColumns: List[ColumnNode], expressionNode: ExpressionNode, targetColumn: ColumnNode): Graph = {
    val expressionGraph: Graph = expressionsGraph(sourceColumns, expressionNode, "SELECT")
    val exprTargetEdge = Edge(expressionNode, targetColumn, Map("expressionType" -> "SELECT"))
    val nodes: Set[Node] = expressionGraph.nodes + expressionNode + targetColumn
    val edges: Set[Edge] = expressionGraph.edges + exprTargetEdge
    val graph = Graph(nodes, edges)
    graph
  }

  def expressionsGraph(sourceColumns: List[ColumnNode], expressionNode: ExpressionNode, context: String): Graph = {
    val sourceExprEdge: Set[Edge] = sourceColumns.map(srcColumn => {
      // source primary Relation
      val sourceColumnEdge =
        if (srcColumn.node.isDefined) {
          val node: Node = srcColumn.node.get
          // edge from sourceNode
          val srcEdge = Edge(node, srcColumn, Map("expressionType" -> context))
          Some(srcEdge)
        } else None
      val columnExprEdge = Edge(srcColumn, expressionNode, Map("expressionType" -> context))
      if (sourceColumnEdge.isDefined) {
        Set(sourceColumnEdge.get, columnExprEdge)
      } else Set(columnExprEdge)
    }).toSet.flatten

    // in case its subquery and from clause is not visited.
    val relationNodes: Set[Node] = sourceColumns.map(_.node).filter(_.isDefined).map(_.get).toSet
    val nodes: Set[Node] = sourceColumns.toSet ++ relationNodes + expressionNode
    val edges: Set[Edge] = sourceExprEdge
    Graph(nodes, edges)
  }

  def columnToNode(column: Column): ColumnNode = {
    val node: Option[Node] =
      if (column.relation.isDefined) {
        val primaryRelation = column.relation.get
        val primaryRelationNode: Option[Node] = primaryRelation match {
          case ctx: TableName => {
            val tableNode: Option[Node] =
              if (ctx.table.alias.isDefined) {
                val table = aliasMapping.get(ctx.table.alias.get)
                // TODO revisit. in case of subquery , havent visited from clause, just extracted all columns
                if (table.isDefined) {
                  table
                } else Some(TableNode(ctx.table.name, ctx.table.schema, None))
              } else Some(TableNode(ctx.table.name, ctx.table.schema, None))
            tableNode
          }
          case ctx: AliasedQuery => {
            aliasMapping.get(ctx.alias)
          }
        }
        primaryRelationNode
      } else None
    ColumnNode(column.name, node)
  }

  def expressionsGraph(expressions: List[Expression], context: String): Graph = {
    val graph: Graph = expressions.foldLeft(Graph.empty)((graph, expression) => {
      val expressionNode: ExpressionNode = ExpressionNode("", expression)
      val columns: List[Column] = ASTTreeTravasal.traverseExpression(expression)
      val columnNodes: List[ColumnNode] = columns.map(col => columnToNode(col))
      val currGraph = expressionsGraph(columnNodes, expressionNode, context)
      val nodes: Set[Node] = graph.nodes ++ currGraph.nodes
      val edges: Set[Edge] = graph.edges ++ currGraph.edges
      Graph(nodes, edges)
    })
    graph
  }

  def primaryRelationGraph(primaryRelation: PrimaryRelation): Graph = {
    val graph: Graph = primaryRelation match {
      case table: TableName => {
        aliasMapping += (table.table.alias.getOrElse("") -> TableNode(table.table.name, table.table.schema, None))
        val tableNode: TableNode = TableNode(table.table.name, table.table.schema, None)
        Graph(Set(tableNode), Set.empty[Edge])
      }
      case alias: AliasedQuery => {
        val querySpec: QuerySpec = alias.query.querySpec
        val graph: Graph = querySpecGraph(querySpec, None)
        val toNodes: Set[Node] = findTargetNode(graph)
        val columnNodes: Set[ColumnNode] = toNodes.filter(node => node.isInstanceOf[ColumnNode]).map(node => node.asInstanceOf[ColumnNode])
        val aliasNode: AliasNode = AliasNode("", alias.alias, Some(columnNodes))
        aliasMapping.clear()
        aliasMapping += (alias.alias -> aliasNode)
        val newEdges = addEdge(toNodes, aliasNode, "ALIAS")
        // reset the alias-mapping because aliases will not be relevant outside of this scope
        val totalEdges: Set[Edge] = graph.edges ++ newEdges
        val totalNodes: Set[Node] = graph.nodes + aliasNode
        Graph(totalNodes, totalEdges)
      }
    }
    graph
  }

  def relationGraph(relation: Relation): Graph = {
    val primaryGraph: Graph =
      if (relation.primaryRelation.isDefined) {
        val primaryRelation: PrimaryRelation = relation.primaryRelation.get
        val graph = primaryRelationGraph(primaryRelation)
        graph
      } else Graph.empty
    val joinGraph: Graph =
      if (relation.join.isDefined) {
        relation.join.get.foldLeft(Graph.empty)((graph, join) => {
          val output = primaryRelationGraph(join.primaryRelation)
          val joinType = join.joinType
          val joinCriteria: Expression = join.joinCriteria
          val expressionNode: ExpressionNode = ExpressionNode("", joinCriteria)
          val columns: List[Column] = ASTTreeTravasal.traverseExpression(joinCriteria)
          val columnNodes: List[ColumnNode] = columns.map(col => columnToNode(col))
          val currGraph = expressionsGraph(columnNodes, expressionNode, joinType)
          val nodes: Set[Node] = graph.nodes ++ currGraph.nodes ++ output.nodes
          val edges: Set[Edge] = graph.edges ++ currGraph.edges ++ output.edges
          Graph(nodes, edges)
        })
      } else Graph.empty

    val nodes = primaryGraph.nodes ++ joinGraph.nodes
    val edges = primaryGraph.edges ++ joinGraph.edges
    Graph(nodes, edges)
  }

  def querySpecGraph(querySpec: QuerySpec, targetNode: Option[Node]): Graph = {
    // from Graph
    val fromGraph: Graph =
      if (querySpec.fromClause.isDefined) {
        val relations: List[Relation] = querySpec.fromClause.get
        relations.foldLeft(Graph.empty)((graph, relation) => {
          val currGraph: Graph = relationGraph(relation)
          val nodes = graph.nodes ++ currGraph.nodes
          val edges = graph.edges ++ currGraph.edges
          Graph(nodes, edges)
        })
      } else Graph.empty

    // where graph
    val whereGraph: Graph =
      if (querySpec.whereClause.isDefined) {
        expressionsGraph(querySpec.whereClause.get, "WHERE")
      } else Graph.empty

    //  aggregate Graph
    val aggregateGraph: Graph =
      if (querySpec.aggregationClause.isDefined) {
        expressionsGraph(querySpec.aggregationClause.get, "GROUP BY")
      } else Graph.empty

    // select clause
    val namedExpressionSeq: List[NamedExpression] = querySpec.selectClause.namedExpressionSeq
    val selectGraph: Graph = namedExpressionSeq.foldLeft(Graph.empty)((graph, namedExpr) => {
      val namedExprGraph: (List[Column], Expression, Column) = ASTTreeTravasal.traverseNamedExpression(namedExpr)
      val columNodes: List[ColumnNode] = namedExprGraph._1.map(col => columnToNode(col))
      val expressionNode: ExpressionNode = ExpressionNode("", namedExprGraph._2)
      val currGraph: Graph = namedExpressionGraph(columNodes, expressionNode, ColumnNode(namedExprGraph._3.name, targetNode))
      val nodes: Set[Node] = graph.nodes ++ currGraph.nodes
      val edges: Set[Edge] = graph.edges ++ currGraph.edges
      Graph(nodes, edges)
    })

    val nodes = fromGraph.nodes ++ whereGraph.nodes ++ aggregateGraph.nodes ++ selectGraph.nodes
    val edges = fromGraph.edges ++ whereGraph.edges ++ aggregateGraph.edges ++ selectGraph.edges
    Graph(nodes, edges)
  }

  def createTableGraph(create: Create): Graph = {
    val targetTable: Table = create.table
    val tableNode = TableNode(targetTable.name, targetTable.schema, None)
    val query: Query = create.query.get
    val queryGraph: Graph = querySpecGraph(query.querySpec, Some(tableNode))
    // create edge between leafNodes in Graph to this targetNode
    val toNodes: Set[Node] = findTargetNode(queryGraph)
    val newEdges: Set[Edge] = toNodes.map(node => {
      Edge(node, tableNode, Map("expressionType" -> "CREATE"))
    })
    val totalEdges: Set[Edge] = queryGraph.edges ++ newEdges
    val totalNodes: Set[Node] = queryGraph.nodes + tableNode
    Graph(totalNodes, totalEdges)
  }

  def queryGraph(query: Query): Graph = {
    val withClauseGraph: Graph =
      if (query.withClause.isDefined) {
        val list: List[NamedQuery] = query.withClause.get
        aliasGraph(list)
      } else Graph.empty

    val querySpec: QuerySpec = query.querySpec
    val specGraph: Graph = querySpecGraph(querySpec, None)

    val orderByGraph: Graph =
      if (query.queryOrganization.isDefined) {
        val expressions: List[Expression] = query.queryOrganization.get.orderBy
        expressionsGraph(expressions, "ORDERBY")
      } else Graph.empty

    val nodes = withClauseGraph.nodes ++ specGraph.nodes ++ orderByGraph.nodes
    val edges = withClauseGraph.edges ++ specGraph.edges ++ orderByGraph.edges
    Graph(nodes, edges)
  }

  def generateSqlFlow(statement: Statement): Graph = {
    statement match {
      case create: Create => {
        createTableGraph(create)
      }
      case query: Query => {
        queryGraph(query)
      }
    }

  }

  def aliasGraph(list: List[NamedQuery]): Graph = {
    val aliasGraph: Graph = list.foldLeft(Graph.empty)((graph, namedQuery) => {
      val alias = namedQuery.alias.get
      val querySpec: QuerySpec = namedQuery.selectQueryLineage
      val aliasNode: AliasNode = AliasNode("", alias, None)
      val graph: Graph = querySpecGraph(querySpec, Some(aliasNode))
      val toNodes: Set[Node] = findTargetNode(graph)
      val columnNodes: Set[ColumnNode] = toNodes.filter(node => node.isInstanceOf[ColumnNode]).map(node => node.asInstanceOf[ColumnNode])
      val aliasNodeWithColumns: AliasNode = aliasNode.copy(columnNodes = Some(columnNodes))
      // Reset aliasMapping as aliases will not be used outside this scope
      aliasMapping.clear()
      aliasMapping += (alias -> aliasNodeWithColumns)
      val newEdges = addEdge(toNodes, aliasNodeWithColumns, "WITH")
      val totalEdges: Set[Edge] = graph.edges ++ newEdges
      val totalNodes: Set[Node] = graph.nodes + aliasNodeWithColumns
      Graph(totalNodes, totalEdges)
    })
    aliasGraph
  }

  private def addEdge(source: Set[Node], target: Node, context: String): Set[Edge] = {
    val edges: Set[Edge] = source.map(node => {
      Edge(node, target, Map("statementType" -> context))
    })
    edges
  }

  private def addEdge(source: Node, target: Set[Node], context: String): Set[Edge] = {
    val edges: Set[Edge] = target.map(node => {
      Edge(source, node, Map("statementType" -> context))
    })
    edges
  }

  private def findTargetNode(graph: Graph): Set[Node] = {
    val edges: Set[Edge] = graph.edges
    val toNodes = edges.map(edge => edge.target)
    val fromNodes = edges.map(edge => edge.source)
    val leafNodes = toNodes.diff(fromNodes)
    leafNodes
  }

  private def findSourceNode(graph: Graph): Set[Node] = {
    val edges: Set[Edge] = graph.edges
    val toNodes = edges.map(edge => edge.target)
    val fromNodes = edges.map(edge => edge.source)
    val rootNodes = fromNodes.diff(toNodes)
    rootNodes
  }

}
