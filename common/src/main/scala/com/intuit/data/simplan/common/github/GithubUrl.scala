package com.intuit.data.simplan.common.github

import com.intuit.data.simplan.global.exceptions.SimplanException

import java.net.URI
import scala.util.Try

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 31-Mar-2022 at 5:44 PM
  */

/**
  * Example : "https://github.intuit.com/Superglue/test_scripts/blob/master/spark_jobs/thomas/spark_test.sql"
  *
  * url - https://github.intuit.com/Superglue/test_scripts/blob/master/spark_jobs/thomas/spark_test.sql <br \>
  * host - github.intuit.com<br \>
  * owner - Superglue<br \>
  * repo - test_scripts<br \>
  * branch - master<br \>
  * path - spark_jobs/thomas/spark_test.sql<br \>
  * scheme = https<br \>
  *
  * @param url
  * @param host
  * @param owner
  * @param repo
  * @param branch
  * @param path
  * @param scheme
  */
case class GithubUrl(url: String, host: String, owner: String, repo: String, branch: String, path: String, scheme: String) {
  def getDocumentationUrl: Option[String] = Try(s"$scheme://$host/$owner/$repo/blob/$branch/.documentation/$path.json").toOption
}

object GithubUrl {

  @deprecated
  def apply(url: String, githubBaseUrl: String): GithubUrl = {
    apply(url)
  }

  def apply(url: String): GithubUrl = {
    val uri = new URI(url)
    val refinedUrl = uri.getRawPath
    val splitted = refinedUrl.split("/")
    if (splitted.size >= 5) {
      val path = splitted.drop(5).mkString("/")
      GithubUrl(url, uri.getHost, splitted(1), splitted(2), splitted(4), path, uri.getScheme)
    } else throw new SimplanException("Github Url Parsing Error : $url")
  }

}
