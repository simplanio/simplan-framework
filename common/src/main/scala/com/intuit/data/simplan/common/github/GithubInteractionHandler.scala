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

package com.intuit.data.simplan.common.github

import com.intuit.data.simplan.common.rest.SimplanRestClient
import com.intuit.data.simplan.common.utils.GithubUtils
import com.intuit.data.simplan.common.utils.GithubUtils.decode
import org.eclipse.egit.github.core._
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.{ContentsService, DataService, PullRequestService}

import java.util
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/** @author Abraham, Thomas - tabraham1
  *         Created on 09-Oct-2023 at 9:27 AM
  */

class GithubInteractionHandler(
    host: String,
    token: String,
    titlePrefix: String = "[Simplan] - ",
    branchPrefix: String = "simplan",
    scheme: String = "https",
    port: Int = -1,
    defaultBranch: String = "master"
) extends Serializable {
  private lazy val restClient: SimplanRestClient = SimplanRestClient(Map("Authorization" -> s"token $token"), 10)
  private lazy val client = new GitHubClient(host, port, scheme).setOAuth2Token(token)
  private lazy val contentsService = new ContentsService(client)
  private lazy val prService = new PullRequestService(client)
  private lazy val dataService = new DataService(client)
  private lazy val commitServiceExtention = new CommitServiceExtention(client)

  def getFileContents(owner: String, repoName: String, relativePath: String): Option[GithubFileContent] = getFileContents(owner, repoName, relativePath, None)

  def getFileContents(owner: String, repoName: String, relativePath: String, refs: Option[String]): Option[GithubFileContent] = {
    val repo = new RepositoryId(owner, repoName)
    val contents = refs match {
      case Some(value) => contentsService.getContents(repo, relativePath, value)
      case None        => contentsService.getContents(repo, relativePath)
    }
    if (contents.size() > 0) {
      val decodedContent = decode(contents.get(0).getContent.replaceAll("\n", "").stripLineEnd)
      Some(new GithubFileContent().setContent(decodedContent).setOwner(owner).setRepo(repoName).setPath(relativePath).setBranch(refs.getOrElse(defaultBranch)).setSha(contents.get(0).getSha));
    } else
      None
  }

  def getFileContents(url: String, ref: Option[String]): GithubFileContent = {
    val gitUrl = GithubUrl(url)
    getFileContents(gitUrl.owner, gitUrl.repo, gitUrl.path, ref).get
  }

  def getFileContents(url: String): GithubFileContent = {
    val gitUrl = GithubUrl(url)
    getFileContents(gitUrl.owner, gitUrl.repo, gitUrl.path,Option(gitUrl.branch)).get
  }

  def getPullRequestById(owner: String, repoName: String, id: Int): Option[PullRequest] = {
    val repo = new RepositoryId(owner, repoName)
    Try(prService.getPullRequest(repo, id)).toOption
  }

  def commitFiles(commitRequest: GithubCommitRequest): GithubCommitResponse = {
    val fullCommitMessage = s"$titlePrefix ${commitRequest.getCommitMessage}"
    val repo: RepositoryId = new RepositoryId(commitRequest.getOwner, commitRequest.getRepo)
    val allEntries: List[TreeEntry] = GithubUtils.createTreeEntries(
      repo,
      dataService,
      Option(commitRequest.getFilesToAddOrModify).getOrElse(new util.HashMap[String, String]()).asScala.toMap,
      Option(commitRequest.getFilesToDelete).getOrElse(new util.ArrayList[String]()).asScala.toList
    )
    val user: CommitUser = GithubUtils.createCommitUser(commitRequest.getUserName, commitRequest.getUserEmail)

    //If Branch name is not provided
    if (!commitRequest.getBranch.isPresent) {
      return GithubUtils.createBranchAndCommit(dataService, Try(commitRequest.getBranch.get()).toOption, commitRequest.getBaseBranch, fullCommitMessage, repo, allEntries, user, branchPrefix)
    }

    Try(dataService.getReference(repo, s"refs/heads/${commitRequest.getBranch.get}")) match {
      case Success(value) => GithubUtils.commitToExistingBranch(dataService, fullCommitMessage, repo, allEntries, user, value)
      case Failure(_)     => GithubUtils.createBranchAndCommit(dataService, Option(commitRequest.getBranch.get()), commitRequest.getBaseBranch, fullCommitMessage, repo, allEntries, user, branchPrefix)
    }
  }

  def createPR(owner: String, repoName: String, prMessage: String, headBranch: String, baseBranch: String = "master"): Option[PullRequest] = {
    val repo: RepositoryId = new RepositoryId(owner, repoName)
    val fullPRMessage = s"$titlePrefix $prMessage"

    val prHead: PullRequestMarker = new PullRequestMarker().setLabel(headBranch)
    val prBase = new PullRequestMarker().setLabel(baseBranch)

    val pullRequest = new PullRequest().setTitle(fullPRMessage).setHead(prHead).setBase(prBase)
    Try(prService.createPullRequest(repo, pullRequest)).toOption
  }

  def mergePR(owner: String, repoName: String, commitMessage: String, pullRequest: PullRequest): MergeStatus = {
    val repo = new RepositoryId(owner, repoName)
    Thread.sleep(1000); // giving github some time to check the PR that was just created
    val status = prService.merge(repo, pullRequest.getNumber, commitMessage)
    Thread.sleep(3000); // Waiting for PR for be merged
    if (!status.isMerged) {
      throw new Exception(s"Could not automatically merge ${pullRequest.getTitle}: ${status.getMessage}")
    }
    status
  }

  def mergePR(owner: String, repoName: String, commitMessage: String, pullRequestId: Int): MergeStatus = {
    val pullRequest = getPullRequestById(owner, repoName, pullRequestId)
    mergePR(owner, repoName, commitMessage, pullRequest.get)
  }

  // branchName and filePath inputs can be set to null if not desired
  def getRepoCommits(owner: String, repositoryName: String, branchName: String, filePath: String, pageSize: Int, pageNumber: Int) = {
    val repositoryId = new RepositoryId(owner, repositoryName)
    try {
      commitServiceExtention.pageCommits(repositoryId, branchName, filePath, pageSize, pageNumber)
    } catch {
      case e: Exception =>
        throw new Exception(s"Exception occurred when getting github repo commits by branch: ${e.getMessage}")
    }
  }

}

object GithubInteractionHandler {

  def apply(host: String, token: String): GithubInteractionHandler = {
    new GithubInteractionHandler(host, token)
  }

  def apply(host: String, token: String, titlePrefix: String = "[Simplan] - ", branchPrefix: String = "simplan", defaultBranch: String = "master"): GithubInteractionHandler = {
    new GithubInteractionHandler(host, token, titlePrefix, branchPrefix, defaultBranch)
  }
}
