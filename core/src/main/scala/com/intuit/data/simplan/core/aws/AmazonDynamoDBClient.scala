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

package com.intuit.data.simplan.core.aws

import com.amazonaws.auth.InstanceProfileCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.auth.AWSCredentialsProvider
import com.intuit.data.simplan.common.config.SimplanAppContextConfiguration

case class AmazonDynamoWrapper(amazonDynamoDB:AmazonDynamoDB) extends Serializable

class AmazonDynamoDBClient(credentials:AWSCredentialsProvider, region:String) extends Serializable {

  lazy val amazonDynamoDB:AmazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
    .withCredentials(credentials)
    .withRegion(region)
    .build()
}

object AmazonDynamoDBClient extends Serializable {
  def apply(simplanConfiguration: SimplanAppContextConfiguration):AmazonDynamoDB = {
   val awsAuthConfig:AWSAuthConfig = simplanConfiguration.getSystemConfigAs[AWSAuthConfig]("aws")
   val credentials:AWSCredentialsProvider = AWSAuth(awsAuthConfig.auth)
    val client = new AmazonDynamoDBClient(credentials, simplanConfiguration.application.region.getOrElse("us-west-2"))
    client.amazonDynamoDB
  }
}
