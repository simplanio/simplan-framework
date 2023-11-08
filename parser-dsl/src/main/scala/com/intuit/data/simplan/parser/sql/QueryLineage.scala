package com.intuit.data.simplan.parser.sql


//trait SqlLineage extends Serializable
// may not have a target column. So target is Optional
// there can be multiple columns from source table contributing to a target table
//case class ColumnLineage(source:List[Column], target: Option[Column], expression:Expression)
//case class QueryLineage(input:List[Table], columnLineage: List[ColumnLineage]) extends SqlLineage
//case class CreateTableLineage(output:Table, queryLineage:QueryLineage) extends SqlLineage






// transformation between source and target column
//case class ColumnSpec(name: String, expression: Expression, sourceName: String, sourceTable: Option[TableName])
//case class ColumnLineage(sourceColumn: Column,targetColumn:String, expression:Expression)


// A source column needed, may not be avaialble in transformation
//case class SelectQueryLineage(inputTable:List[Table], transformations: List[NamedExpression], inputExpressions:List[Expression])
//case class InputQueryLineage(selectQueryLineage: SelectQueryLineage, namedQuery:Option[List[NamedQuery]])

//case class QueryLineage(outputTables: Table, lineage:InputQueryLineage)

//case class ColumnLineage(sourceColumn: Column,targetColumn:String, expression:Expression)

//case class Lineage(outputTables: Option[Table], inputTables:List[Table], columnLineage:ColumnLineage, aggOrgExpression:List[Expression])


