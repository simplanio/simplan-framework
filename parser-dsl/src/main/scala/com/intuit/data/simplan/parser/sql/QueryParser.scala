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


// Expression
trait Expression extends Serializable
case class ValueExpression(primaryQueryExpression: PrimaryExpression) extends Expression
case class ComparisonExpression(left: Expression, comparision: String = "", right: Expression) extends Expression
case class ArithmeticExpression(left: Expression, operator: String = "", right: Expression) extends Expression

case class LogicalExpression(left: Expression, operator: String = "", right:Expression) extends Expression

// PrimaryExpression
trait PrimaryExpression extends Serializable
trait WindowSpec extends Serializable
case class WindowDef(windowType:String, partByExps: List[Expression], orderByExps:List[Expression]) extends WindowSpec
case class Dereference(column: Column, alias: Option[String] = None) extends PrimaryExpression
case class FunctionCall(functionName: String, expression:Option[Expression], alias:Option[String], windowSpec:Option[WindowSpec]) extends PrimaryExpression
//case class ColumnReference(identifier: String) extends PrimaryExpression
case class ColumnReference(column:Column) extends PrimaryExpression
case class ColumnReferences(columns:List[Column]) extends PrimaryExpression

case class Constant(literal: String) extends PrimaryExpression
case class SearchedCase(when:List[Expression], thenStmt: List[Expression], elseStmt: Option[Expression] ) extends PrimaryExpression
case class Cast(expression:Expression) extends PrimaryExpression
case class Query(querySpec:QuerySpec, withClause: Option[List[NamedQuery]], queryOrganization:Option[QueryOrganization]) extends Statement
case class SubQuery(query:Query) extends PrimaryExpression
// TODO check this
case class Paranthesis(expression:Expression) extends PrimaryExpression

// From Clause
trait PrimaryRelation extends Serializable
case class TableName(table:Table) extends PrimaryRelation
case class AliasedQuery(query:Query, alias: String) extends PrimaryRelation

// Table
case class Table(name: String, schema: Option[String], alias:Option[String])
case class Column(name:String,relation:Option[PrimaryRelation])

case class JoinRelation(primaryRelation:PrimaryRelation, joinCriteria:Expression, joinType:String)
case class Relation(primaryRelation: Option[PrimaryRelation] , join: Option[List[JoinRelation]])

trait Statement extends Serializable

// Select Clause
case class NamedExpressionSeq(namedExpressionSeq: List[NamedExpression])
case class QuerySpec(selectClause: NamedExpressionSeq, fromClause:Option[List[Relation]] ,
                     whereClause:Option[List[Expression]], aggregationClause:Option[List[Expression]])
case class QueryOrganization(orderBy:List[Expression])
case class NamedExpression(expression: Expression, alias: Option[String] = None)
case class NamedQuery(selectQueryLineage: QuerySpec, alias: Option[String])


// Create Table
case class Create(table:Table, query:Option[Query]) extends Statement

case class MultipartIdentifier(text: String) {
  def parts: List[String] = text.split("\\.").toList
}


