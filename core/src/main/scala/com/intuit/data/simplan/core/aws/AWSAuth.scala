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

import com.amazonaws.auth.{AWSCredentialsProvider, AWSStaticCredentialsProvider, BasicAWSCredentials, BasicSessionCredentials, InstanceProfileCredentialsProvider}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder

case class AWSAuthConfig(auth:AWSAuthType)

 trait AWSAuthType extends Serializable {
   def authType:String
 }
case class InstanceAWSAuthConfig(authType:String) extends AWSAuthType
case class BasicAWSAuthConfig(authType:String, accessKey:String, secretKey:String, sessionToken: String) extends AWSAuthType


class AWSAuth(auth:AWSAuthType) {

  val credentials: AWSCredentialsProvider ={
    auth.authType match {
      case "basic" => {
        val basic = auth.asInstanceOf[BasicAWSAuthConfig]
        new AWSStaticCredentialsProvider(new BasicSessionCredentials(basic.accessKey, basic.secretKey, basic.sessionToken));
      }
      case "instance" => {
        val instance = auth.asInstanceOf[InstanceAWSAuthConfig]
        new InstanceProfileCredentialsProvider(false);
      }
      case _ => {
        throw new Exception("Unknown authentication Type")
      }
    }
  }

}

object AWSAuth{
  def apply(auth:AWSAuthType):AWSCredentialsProvider={
    (new AWSAuth(auth)).credentials
  }
}

