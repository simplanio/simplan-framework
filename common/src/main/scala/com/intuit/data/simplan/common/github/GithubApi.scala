package com.intuit.data.simplan.common.github

import com.intuit.data.simplan.common.config.SimplanAppContextConfiguration
import com.intuit.data.simplan.common.rest.SimplanRestClient
import org.apache.commons.io.FileUtils

import java.io.{File, InputStream}
import scala.util.{Failure, Success}

/** @author Abraham, Thomas - tabraham1
  *         Created on 24-Mar-2022 at 1:10 PM
  */

case class GithubFileDetails(sha: String)

class GithubApi(token: String, githubApiBaseUrl: String) {
  private val restClient: SimplanRestClient = SimplanRestClient(Map("Authorization" -> s"token $token"), 10)

  @deprecated
  def getRawData(url: String): String = {
    val parsedUrl = GithubUrl(url)
    val getRawUrl = s"""${parsedUrl.scheme}://${parsedUrl.host}/raw/${parsedUrl.owner}/${parsedUrl.repo}/${parsedUrl.branch}/${parsedUrl.path}"""
    restClient.get[String](getRawUrl) match {
      case Success(value)     => value._1
      case Failure(exception) => throw exception
    }
  }

  def getDocumentationFilePathForSql(url: String): String = GithubUrl(url).getDocumentationUrl.orNull

  def downloadRepository(owner: String, repo: String, branch: String, path: String): Unit = {

    val getRawUrl = s"""$githubApiBaseUrl/repos/$owner/$repo/zipball/$branch"""
    val in: InputStream = restClient.downloadRepository(getRawUrl)
    FileUtils.copyInputStreamToFile(in, new File(path))

  }
}

object GithubApi {

  def apply(token: String, githubApiBaseUrl: String = "https://github.intuit.com/api/v3"): GithubApi = {
    new GithubApi(token, githubApiBaseUrl)
  }

  def apply(simplanConfiguration: SimplanAppContextConfiguration): GithubApi = {
    val ghSystemConfig = simplanConfiguration.getSystemConfigAs[GithubSystemConfig]("github")
    new GithubApi(ghSystemConfig.token.resolve, ghSystemConfig.githubBaseUrl)
  }
}
