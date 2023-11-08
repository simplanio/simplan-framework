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

package com.intuit.data.simplan.parser.sql


import com.intuit.data.simplan.parser.grammer.sparksql.{SparkSqlBaseBaseVisitor, SparkSqlBaseParser}
import com.intuit.data.simplan.parser.grammer.sparksql.SparkSqlBaseParser._
import com.intuit.data.simplan.parser.grammer.sparksql.{SparkSqlBaseBaseVisitor, SparkSqlBaseParser}
import com.intuit.data.simplan.parser.sql.catalog.{MetastoreCatalogProvider, TableFieldSchema}
import org.antlr.v4.runtime.tree.TerminalNode
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters.asScalaBufferConverter

// Expression->PrimaryExpression->Expression
/** As AstBuilder that converts an ANTLR4 ParseTree into a ASTTree and returns top level Statement
  *                                     Statement
  *                                       /   \
  *                                   Create   \
  *                                   /    \    \
  *                                 Table       Query
  *                                          /    |     \
  *                                     With   Select    OrderBy
  *                                         /  |    |  \     
  *                                  Named  Where From Agg
  *                                       |
  *                                  Expression .
  *                                       |
  *                                  Primary
  *                                 Expression
  *                                       |
  *                                 Expression
  *                                     ...
  *
  */
class SparkSqlQueryParserVisitor(catalog:MetastoreCatalogProvider) extends SparkSqlBaseBaseVisitor[Any] {

  // alias and relation mapping. Relation can be table or subquery
  val aliasMapping = scala.collection.mutable.HashMap.empty[String,PrimaryRelation]
  // table mapping
  val tableMapping = scala.collection.mutable.HashMap.empty[String,TableName]

  lazy val logger:Logger = LoggerFactory.getLogger(classOf[SparkSqlBaseBaseVisitor[Any]])


  override def visitSingleMultipartIdentifier(ctx: SparkSqlBaseParser.SingleMultipartIdentifierContext): Any = {
    super.visitSingleMultipartIdentifier(ctx)
  }
  override def visitUnquotedIdentifier(ctx: SparkSqlBaseParser.UnquotedIdentifierContext): String = {
    if (ctx.IDENTIFIER() != null){
      ctx.IDENTIFIER().getText
    }else ""
  }

  override def visitQuotedIdentifierAlternative(ctx: SparkSqlBaseParser.QuotedIdentifierAlternativeContext): Any = {
    ctx.quotedIdentifier().BACKQUOTED_IDENTIFIER().getText
  }

  override def visitSelectClause(ctx: SparkSqlBaseParser.SelectClauseContext): List[NamedExpression] = visitNamedExpressionSeq(ctx.namedExpressionSeq())

  override def visitNamedExpressionSeq(ctx: SparkSqlBaseParser.NamedExpressionSeqContext): List[NamedExpression] = ctx.namedExpression().asScala.map(each => visitNamedExpression(each)).toList

  // can have expression,
  override def visitNamedExpression(ctx: SparkSqlBaseParser.NamedExpressionContext):NamedExpression = {
    val booleanExpressionContext: BooleanExpressionContext = ctx.expression().booleanExpression()
    val expression:Expression = visitExpression(ctx.expression())
    val alias: Option[String] = if (ctx.errorCapturingIdentifier() != null) {
      val as = visit(ctx.errorCapturingIdentifier())
      if (as != null)
        Some(as.asInstanceOf[String])
      else None
    } else None
    NamedExpression(expression,alias)
  }

  override def visitErrorCapturingIdentifier(ctx: SparkSqlBaseParser.ErrorCapturingIdentifierContext): String = visitIdentifier(ctx.identifier())

  override def visitExpression(ctx: SparkSqlBaseParser.ExpressionContext): Expression = {
      handleBooleanExpressionContext(ctx.booleanExpression())
  }

  override def visitDereference(ctx: SparkSqlBaseParser.DereferenceContext): PrimaryExpression = {
    val identifier = visitIdentifier(ctx.identifier())
    val colReference = handlePrimaryExpression(ctx.primaryExpression()).asInstanceOf[ColumnReference]
    // in case of dereference , column always have alias in front
    val tableAliasOpt:Option[PrimaryRelation] = aliasMapping.get(colReference.column.name)
    val table = if(tableAliasOpt.isDefined){
      tableAliasOpt
    } else if (tableMapping.get(colReference.column.name).isDefined) {
      tableMapping.get(colReference.column.name)
    }else None
    val column = Column(identifier, table)
    Dereference(column,None)
  }


  override def visitFunctionName(ctx: SparkSqlBaseParser.FunctionNameContext):String = {
    val name = visit(ctx.qualifiedName()).asInstanceOf[String]
    name
  }

  override def visitFunctionCall(ctx: SparkSqlBaseParser.FunctionCallContext): PrimaryExpression = {
    val functionName = visitFunctionName(ctx.functionName())
    val expression: Option[Expression] = if (ctx.expression != null){
      Some(visitExpression(ctx.expression))
    }else None
    val windowSpec = if (ctx.windowSpec() !=null){
        handleWindowContext(ctx.windowSpec())
    } else None
    FunctionCall(functionName, expression, None, None)
  }

  // Improve this
  def handleWindowContext(ctx: SparkSqlBaseParser.WindowSpecContext): WindowSpec ={
    ctx match {
      case window: SparkSqlBaseParser.WindowDefContext => {
        val partByExpressions:List[Expression] =
          window.expression().asScala.toList.map(exp=>visitExpression(exp))
        val sortItems:List[SortItemContext] = window.sortItem().asScala.toList
        val sortExpressions:List[Expression] = sortItems.map(sortItem=>{
          visitExpression(sortItem.expression())
        })
        WindowDef("PARTIONBY", partByExpressions, sortExpressions)
      }
      case _ => throw new Exception("Unknown Window Type")
    }

  }



  private def handlePrimaryExpression(ctx: SparkSqlBaseParser.PrimaryExpressionContext): PrimaryExpression = {
    ctx match {
      case c: ColumnReferenceContext => visitColumnReference(c)
      case c: DereferenceContext     => visitDereference(c)
      case c: FunctionCallContext     => visitFunctionCall(c)
      case c: ConstantDefaultContext => visitConstantDefault(c)
      case c: SearchedCaseContext => {
        val elseExpr = if (c.elseExpression !=null){
          Some(visitExpression(c.elseExpression))
        } else None
        val whenExprResult = c.whenClause().asScala.map(whenClauseContext =>{
             val condition = whenClauseContext.condition
             val result = whenClauseContext.result
             val conditionExprs:Expression = visitExpression(condition)
             val resultExprs:Expression = visitExpression(result)
          (conditionExprs, resultExprs)
          })
        val whenExpr:List[Expression] = whenExprResult.map(_._1).toList
        val thenExpr:List[Expression] = whenExprResult.map(_._2).toList
        SearchedCase(whenExpr, thenExpr, elseExpr)
      }
      case c: CastContext => Cast(visitExpression(c.expression()))
      case c: SubstringContext => {
        c.valueExpression().asScala.map(valueExpressionCtx =>{
          val primaryExpression = valueExpressionCtx.asInstanceOf[ValueExpressionDefaultContext].primaryExpression()
          handlePrimaryExpression(primaryExpression)
        }).head
      }
      case c:StarContext => {
        val tables:List[TableName] = tableMapping.values.toList
        val primaryRelations:List[PrimaryRelation] = if (c.qualifiedName() != null){
          val identifier:String = visit(c.qualifiedName()).asInstanceOf[String]
          if(tableMapping.get(identifier).isDefined){
            List(tableMapping.get(identifier).get)
          } else if(aliasMapping.get(identifier).isDefined){
            List(aliasMapping.get(identifier).get)
          }
          else List.empty[TableName]
        } else {
          tables ++ aliasMapping.values
        }

        val schema:List[TableFieldSchema] = primaryRelations.map(primaryRelation =>{
          primaryRelation match {
            case table:TableName =>{
              val schemaList:List[TableFieldSchema] = catalog.getTableFields(table.table.schema.getOrElse(""), table.table.name)
              schemaList
            }
            case alias:AliasedQuery =>{
              //TODO handle for alias
              List.empty[TableFieldSchema]
            }
          }
        }).flatten

        val cols:List[Column] = schema.map(s=>{
          val schema = s.tableName.split("\\.")(0)
          val name = s.tableName.split("\\.")(1)
          val table = TableName(Table(name,Some(schema),None))
          Column(s.columnName, Some(table))
        })
        ColumnReferences(cols)
      }
      case c:ParenthesizedExpressionContext => {
        val expr = visitExpression(c.expression())
        Paranthesis(expr)
      }
      case c:SubqueryExpressionContext => {
        val queryContext:QueryContext = c.query()
        val query:Query = visitQuery(queryContext)
        SubQuery(query)
      }
      case c:CurrentDatetimeContext => {
        Constant(c.getText)
      }
      case x => {
        throw new Exception("unhandled Primary Exp: " + x.getText)
      }
    }
  }

  override def visitConstantDefault(ctx: SparkSqlBaseParser.ConstantDefaultContext): PrimaryExpression = {
    Constant(ctx.constant().getText)
  }

  override def visitColumnReference(ctx: SparkSqlBaseParser.ColumnReferenceContext): PrimaryExpression = {
    val identifier = visitIdentifier(ctx.identifier())
    // resolve the tablename in dereference
    if (aliasMapping.get(identifier).isDefined || tableMapping.get(identifier).isDefined){
      ColumnReference(Column(identifier,None))
    }else{
      val tables:List[TableName] = tableMapping.values.toList
      // if theres's only 1 table
      if (tables.length == 1){
        val table = tables.head
        ColumnReference(Column(identifier, Some(table)))
      }else {
        // some tables may or may not have alias , so taking full list
        val tablesList  = tables ++ aliasMapping.values.filter(_.isInstanceOf[TableName]).map(_.asInstanceOf[TableName])
        val schema:List[TableFieldSchema] =   tablesList.foldLeft(List.empty[TableFieldSchema])((list, table)=>{
          val schemaList:List[TableFieldSchema] = try{
            catalog.getTableFields(table.table.schema.getOrElse(""), table.table.name)
          }catch {
            case ex:Exception =>{
              logger.error(s"Table ${table.table.schema}.${table.table.name} not found in the metastore", ex)
              List.empty[TableFieldSchema]
            }
          }
          list ++ schemaList
        })

        val tableSchemaOpt:Option[TableFieldSchema] = schema.find(s=>{
          s.columnName.equalsIgnoreCase(identifier)
        })
        if (tableSchemaOpt.isDefined){
          val tname = tableSchemaOpt.get.tableName
          val schema = tableSchemaOpt.get.tableName.split("\\.")(0)
          val name = tableSchemaOpt.get.tableName.split("\\.")(1)
          val table = TableName(Table(name,Some(schema),None))
          ColumnReference(Column(identifier, Some(table)))
        }else ColumnReference(Column(identifier,None))

      }
    }
  }

  override def visitIdentifier(ctx: SparkSqlBaseParser.IdentifierContext): String = {
    val children = ctx.children.asScala
    if (children.size == 1) {
      visit(children.head).asInstanceOf[String]
    } else if (children.size > 1) {
      throw new Exception("In identifier there are more than 1 children, Understand this")
    } else {
      throw new Exception("Unable to determine Identifier")
    }
  }

  override def visitMultipartIdentifier(ctx: SparkSqlBaseParser.MultipartIdentifierContext): MultipartIdentifier = MultipartIdentifier(ctx.getText)

  override def visitTableAlias(ctx: SparkSqlBaseParser.TableAliasContext): String = super.visitTableAlias(ctx).asInstanceOf[String]

  override def visitTableName(ctx: SparkSqlBaseParser.TableNameContext): Table = {
    val parts = visitMultipartIdentifier(ctx.multipartIdentifier()).parts
    val (table, schema) = if (parts.size == 1) (parts.head, None) else (parts(1), Some(parts.head))
    val alias =
      if (ctx.tableAlias() != null) {
        val p = visit(ctx.tableAlias()).asInstanceOf[String]
        if (p != null){
          Some(p)
        }else None
      } else None
    Table(table,schema, alias)

  }

  override def visitFromClause(ctx: SparkSqlBaseParser.FromClauseContext):List[Relation] = {
    ctx.relation().asScala.map(relCtx => visitRelation(relCtx)).toList
  }

  def handleValueExpressionContext(ctx: SparkSqlBaseParser.ValueExpressionContext):Expression = {
    ctx match {
      case valueExpressionCtx:ValueExpressionDefaultContext => {
        val primaryExpressionCntxt:PrimaryExpressionContext = valueExpressionCtx.primaryExpression()
        val primaryExpression = handlePrimaryExpression(primaryExpressionCntxt)
        ValueExpression(primaryExpression)
      }
      case expressionCntxt:ComparisonContext =>{
        val left: SparkSqlBaseParser.ValueExpressionContext = expressionCntxt.left
        val right: SparkSqlBaseParser.ValueExpressionContext = expressionCntxt.right
        val leftExpression:Expression  = handleValueExpressionContext(left)
        val rightExpression:Expression = handleValueExpressionContext(right)
        val operator:String = visitComparisonOperator(expressionCntxt.comparisonOperator())
        ComparisonExpression(leftExpression, operator, rightExpression)
      }
      case expressionCntxt: SparkSqlBaseParser.ArithmeticBinaryContext => {
        val left: SparkSqlBaseParser.ValueExpressionContext = expressionCntxt.left
        val right: SparkSqlBaseParser.ValueExpressionContext = expressionCntxt.right
        val leftExpression:Expression  = handleValueExpressionContext(left)
        val rightExpression:Expression = handleValueExpressionContext(right)
        val operator = expressionCntxt.operator.getText
        ArithmeticExpression(leftExpression, operator, rightExpression)
      }
    }

  }

  private def handleBooleanExpressionContext(ctx: SparkSqlBaseParser.BooleanExpressionContext):Expression = {
    ctx match {
      case c: PredicatedContext  => {
        val valueExpressionCtx = c.valueExpression()
        handleValueExpressionContext(valueExpressionCtx)
      }
      case logicalBinary: LogicalBinaryContext => {
        val left = logicalBinary.left
        val right = logicalBinary.right
        val leftExpr = handleBooleanExpressionContext(left)
        val rightExpr = handleBooleanExpressionContext(right)
        val operator =  logicalBinary.operator.getText
        LogicalExpression(leftExpr, operator, rightExpr)
      }
      case _ => {
        throw new Exception("not supported type")
      }
    }
  }

  override def visitJoinType(ctx: SparkSqlBaseParser.JoinTypeContext):String = {
    if(ctx.LEFT() != null){
      "LEFT JOIN"
    }
    else if (ctx.RIGHT() != null){
      "RIGHT JOIN"
    }
    else if (ctx.FULL() != null){
      "FULL JOIN"
    }
    else if (ctx.INNER() != null){
      "INNER JOIN"
    }
    else if (ctx.SEMI() != null){
      "SEMI JOIN"
    }
    else if (ctx.ANTI()!= null){
      "ANTI JOIN"
    }
    else if (ctx.CROSS()!= null){
      "CROSS JOIN"
    }
    else {
      "JOIN"
    }

  }

  override def visitComparisonOperator(ctx: SparkSqlBaseParser.ComparisonOperatorContext):String ={
    if (ctx.EQ() !=null){
      "EQ"
    }
    else if (ctx.GT() !=null){
      "GT"
    }
    else if(ctx.LT() != null){
      "LT"
    }
    else if(ctx.NEQ() != null){
      "NEQ"
    }
    else if(ctx.LTE() != null){
      "LTE"
    }
    else if(ctx.GTE != null){
      "GTE"
    }
    else if(ctx.NEQJ() != null){
      "NEQJ"
    }
    else if(ctx.NSEQ() != null){
      "NSEQ"
    }
    else {
      "Unknown"
    }

  }

  override def visitJoinRelation(ctx: SparkSqlBaseParser.JoinRelationContext):JoinRelation = {
    val joinCriteriaCxt: JoinCriteriaContext = ctx.joinCriteria()
    val relationPrimaryContext:RelationPrimaryContext = ctx.relationPrimary()
    val primaryRelation = handlePrimaryRelation(relationPrimaryContext)
    val joinType = visitJoinType(ctx.joinType())
    val joinCriteria:Expression = visitJoinCriteria(joinCriteriaCxt)
    JoinRelation(primaryRelation, joinCriteria, joinType)
  }

  override def visitJoinCriteria(ctx: SparkSqlBaseParser.JoinCriteriaContext):Expression = {
    val booleanExpCntxt = ctx.booleanExpression()
    handleBooleanExpressionContext(booleanExpCntxt)
  }

  def handlePrimaryRelation(relationPrimaryContext:RelationPrimaryContext): PrimaryRelation={
    relationPrimaryContext match {
      case ctx:TableNameContext => {
        val table:Table =  visit(ctx).asInstanceOf[Table]
        val primaryRelation:PrimaryRelation =
          // result of with is identified as table
          if (aliasMapping.get(table.name).isDefined){
            val relation:PrimaryRelation = aliasMapping.get(table.name).get
            relation
          }else{
            val tableName:PrimaryRelation = TableName(table)
            if (table.alias.isDefined){
              aliasMapping += (table.alias.get -> tableName)
            }else tableMapping += (table.name -> TableName(table))
            tableName
        }

        primaryRelation
      }
      case ctx:AliasedQueryContext => {
        val queryContext:QueryContext = ctx.query()
        val tableAlias:TableAliasContext = ctx.tableAlias()
        val alias = tableAlias.strictIdentifier().getText
        val query:Query = visitQuery(queryContext)
        val aliasedQuery = AliasedQuery(query,alias)
        aliasMapping += (alias -> aliasedQuery)
        aliasedQuery
      }
      case _ => {
        throw new Exception("Unknown Primary Relation type")
      }
    }
  }

  override def visitRelation(ctx: SparkSqlBaseParser.RelationContext):Relation = {
    val relation = if (ctx.relationPrimary() != null){
      val relationPrimaryContext:RelationPrimaryContext = ctx.relationPrimary()
      val primaryRelation = handlePrimaryRelation(relationPrimaryContext)
      Some(primaryRelation)
    } else None
    val joinRelations:Option[List[JoinRelation]] = if (ctx.joinRelation() != null){
      val exps = ctx.joinRelation().asScala.map(joinRelationContext=>{
        visitJoinRelation(joinRelationContext)
      }).toList
      Some(exps)
    }else None
    Relation(relation, joinRelations)
  }

  def handleGroupByExpression(aggregationClause:AggregationClauseContext):List[Expression] ={
    val groupByExpressions:List[Expression] = aggregationClause.expression().asScala.map(each => {
      val exp = visitExpression(each)
      exp
    }).toList
    groupByExpressions
  }

  def handleWhereExpression(whereClause:WhereClauseContext):List[Expression] ={
    val booleanExpContext:BooleanExpressionContext = whereClause.booleanExpression()
    val whereClaueExpressions= handleBooleanExpressionContext(booleanExpContext)
    List(whereClaueExpressions)
  }

  override def visitCtes(ctx: SparkSqlBaseParser.CtesContext):List[NamedQuery] = {
    val namedQueryContext:List[NamedQueryContext] = ctx.namedQuery().asScala.toList
    namedQueryContext.map(namedCtx => {
      val selectSubQuery:QuerySpec = visit(namedCtx.query().queryTerm().asInstanceOf[QueryTermDefaultContext]).asInstanceOf[QuerySpec]
      val alias: Option[String] = if (namedCtx.errorCapturingIdentifier() != null) {
        val as = visit(namedCtx.errorCapturingIdentifier())
        if (as != null)
          Some(as.asInstanceOf[String])
        else None
      } else None
      val query = Query(selectSubQuery, None, None)
      val aliasedQuery = AliasedQuery(query, alias.getOrElse(""))
      aliasMapping += (alias.getOrElse("") -> aliasedQuery)
      NamedQuery(selectSubQuery,alias)
    })
  }

  def visitQuerySpecification(ctx: SparkSqlBaseParser.QueryContext):Query={
    val namedQuery = if (ctx.ctes() != null){
      val ctexContext:CtesContext = ctx.ctes()
      val list = visitCtes(ctexContext)
      Some(list)
    }else None
    val querySpec = visit(ctx.queryTerm().asInstanceOf[QueryTermDefaultContext]).asInstanceOf[QuerySpec]
    val queryOrganization =  if (ctx.queryOrganization() != null){
      Some(visitQueryOrganization(ctx.queryOrganization()))
    } else None
    Query(querySpec, namedQuery, queryOrganization)
  }

  override def visitQueryOrganization(ctx: SparkSqlBaseParser.QueryOrganizationContext): QueryOrganization = {
    val expressions: List[Expression] = ctx.order.asScala.map(sortItemCntxt =>{
       val exp = visitExpression(sortItemCntxt.expression())
      exp
     }).toList
    QueryOrganization(expressions)
  }

  override def visitQuery(ctx: SparkSqlBaseParser.QueryContext):Query={
    visitQuerySpecification(ctx)
  }

  override def visitSingleStatement(ctx: SparkSqlBaseParser.SingleStatementContext): Statement ={
    val statementContext:StatementContext = ctx.statement()
    statementContext match {
      case ctx:CreateTableContext => visitCreateTable(ctx)
      case ctx:StatementDefaultContext => visitStatementDefault(ctx)
      case _ => throw new Exception("Unknown supported Statement")
    }
  }

  override def visitStatementDefault(ctx: SparkSqlBaseParser.StatementDefaultContext):Query = {
    visitQuery(ctx.query())
  }

  override def visitRegularQuerySpecification(ctx: SparkSqlBaseParser.RegularQuerySpecificationContext): QuerySpec = {
    val fromClause:Option[List[Relation]] = try {
      if (ctx.fromClause() !=null){
        Some(visitFromClause(ctx.fromClause()))
      }else None
    }catch {
      case ex:Exception=>{
        ex.printStackTrace()
        None
      }
    }
    val namedExpressions: List[NamedExpression] = visitSelectClause(ctx.selectClause())
    val tables:List[PrimaryRelation] = aliasMapping.values.toList
    val whereClause:Option[List[Expression]] = try {
      if (ctx.whereClause() != null){
        Some(handleWhereExpression(ctx.whereClause()))
      } else None
    }catch {
      case ex:Exception => {
        ex.printStackTrace()
        None
      }
    }
    val aggClause:Option[List[Expression]] = try {
      if (ctx.aggregationClause() !=null){
        Some(handleGroupByExpression(ctx.aggregationClause()))
      } else None
     }
    catch {
      case ex:Exception => {
        ex.printStackTrace()
        None
      }
    }
    val querySpec = QuerySpec(NamedExpressionSeq(namedExpressions), fromClause, whereClause, aggClause)
    querySpec
  }

  override def visitCreateTableHeader(context: SparkSqlBaseParser.CreateTableHeaderContext):Table = {
    val children = context.children.asScala
    val multiPartIdentifierContext:SparkSqlBaseParser.MultipartIdentifierContext = context.multipartIdentifier()
    val parts = visitMultipartIdentifier(multiPartIdentifierContext).parts
    val (table, schema) = if (parts.size == 1) (parts.head, None) else (parts(1), Some(parts.head))
    Table(table, schema,None)
  }

  override def visitCreateTable(context: SparkSqlBaseParser.CreateTableContext):Create = {
    val tableProvider:CreateTableHeaderContext = context.createTableHeader()
    val createTableClause:CreateTableClausesContext = context.createTableClauses();
    val colTypeList:ColTypeListContext = context.colTypeList();
    val table = visitCreateTableHeader(context.createTableHeader())
    val query = if(context.query() !=null){
      val queryContext:QueryContext  = context.query()
      Some(visitQuerySpecification(context.query()))
    }else None
    Create(table, query)
  }

}
