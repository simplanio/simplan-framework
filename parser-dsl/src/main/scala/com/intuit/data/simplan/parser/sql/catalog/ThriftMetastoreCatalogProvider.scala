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

package com.intuit.data.simplan.parser.sql.catalog


import com.intuit.data.simplan.hive.metastore.HiveMetastoreConnector
import com.intuit.data.simplan.hive.metastore.models.HiveTableField
import com.intuit.data.simplan.hive.metastore.thrift.ThriftHiveMetastoreConnector
import org.apache.hadoop.hive.conf.HiveConf;


class ThriftMetastoreCatalogProvider(hiveMetastore : HiveMetastoreConnector) extends MetastoreCatalogProvider {

  // cache to store tables already read from metastore. This is to reduce the number of metadata calls
  val cache = scala.collection.mutable.HashMap.empty[String,List[TableFieldSchema]]

  def getTableFields(schema:String, name:String): List[TableFieldSchema] ={
    val fullyQualifiedName:String = s"${schema}.${name}"
    if (cache.get(fullyQualifiedName).isDefined){
      cache.get(fullyQualifiedName).get
    }else{
      val fields:List[HiveTableField] = hiveMetastore.getTableFields(schema, name)
      val tableFieldSchema:List[TableFieldSchema] = fields.map(field =>{
        val name:String = field.name
        val fieldType:String = field.fieldType
        val comment:String = field.comment

        TableFieldSchema(fullyQualifiedName, name, fieldType, comment)
      })
      cache += (fullyQualifiedName -> tableFieldSchema)
      tableFieldSchema
    }
  }

}

object ThriftMetastoreCatalogProvider{

  def apply(metastoreUrl:String):ThriftMetastoreCatalogProvider={
    val hiveMetastoreConnector:HiveMetastoreConnector = ThriftHiveMetastoreConnector.apply(metastoreUrl)
    new ThriftMetastoreCatalogProvider(hiveMetastoreConnector)
  }
}
