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

package com.intuit.data.simplan.parser.ast.sparksql

import com.intuit.data.simplan.parser.SparkSqlParser
import com.intuit.data.simplan.parser.ast.ASTTreeBuilder
import com.intuit.data.simplan.parser.sql.Statement
import com.intuit.data.simplan.parser.sql.catalog.MetastoreCatalogProvider
import com.intuit.data.simplan.parser.sql.catalog.ThriftMetastoreCatalogProvider

class SparkSqlASTTreeBuilder(catalog:MetastoreCatalogProvider) extends ASTTreeBuilder{

  def astTree(sql:String):Statement ={
    SparkSqlParser.parse(sql,catalog)
  }

}

object SparkSqlASTTreeBuilder {
  // TODO Configure metastore url
  // Local Url = "thrift://localhost:3319" after setting up ssh tunnel to prod/e2e thrift server
  // Prod Url = "thrift://athena-hms-emr-prd.athena-data-lake-prd.a.intuit.com:9083"
  def apply(metaStoreUrl:String="thrift://athena-hms-emr-prd.athena-data-lake-prd.a.intuit.com:9083"):SparkSqlASTTreeBuilder ={
    val catalog:MetastoreCatalogProvider  = ThriftMetastoreCatalogProvider(metaStoreUrl)
    new SparkSqlASTTreeBuilder(catalog)
  }
}
