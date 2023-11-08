package com.intuit.data.simplan.core.aws

import com.amazonaws.auth.{BasicSessionCredentials, DefaultAWSCredentialsProviderChain}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
import com.amazonaws.services.securitytoken.{AWSSecurityTokenService, AWSSecurityTokenServiceClientBuilder}

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 25-Mar-2022 at 9:45 AM
  */
object AmazonSTS {

  def assumeRole(roleToAssume: String, sessionPrefix: String,region:Regions): BasicSessionCredentials = {
    val stsClient: AWSSecurityTokenService = AWSSecurityTokenServiceClientBuilder.standard.withCredentials(new DefaultAWSCredentialsProviderChain()).withRegion(region).build
    val roleRequest: AssumeRoleRequest = new AssumeRoleRequest()
      .withRoleArn(roleToAssume)
      .withRoleSessionName(sessionPrefix + System.nanoTime.toString)
    val sessionCredentials = stsClient.assumeRole(roleRequest).getCredentials
    val awsCredentials = new BasicSessionCredentials(sessionCredentials.getAccessKeyId, sessionCredentials.getSecretAccessKey, sessionCredentials.getSessionToken)
    awsCredentials
  }
}
