package com.intuit.data.simplan.common.github

import com.fasterxml.jackson.core.`type`.TypeReference
import com.intuit.data.simplan.common.PullRequestModel
import com.intuit.data.simplan.common.config.SimplanAppContextConfiguration
import com.intuit.data.simplan.common.rest.SimplanRestClient
import com.intuit.data.simplan.common.utils.GithubUtils.decode
import com.intuit.data.simplan.global.json.JacksonUtil
import org.eclipse.egit.github.core._
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.{ContentsService, DataService, PullRequestService}

import java.net.URL
import java.util.Arrays.asList
import java.util.Calendar
import scala.collection.JavaConverters._
import scala.util.{Random, Success}

class GithubHandler(host: String, token: String, scheme: String = "https", port: Int = -1, defaultBranch: String = "master") {
  private val restClient: SimplanRestClient = SimplanRestClient(Map("Authorization" -> s"token $token"), 10)
  private val client = new GitHubClient(host, port, scheme).setOAuth2Token(token)

  def mergePullRequest(owner: String, repoName: String, commitMessage: String, pr: Option[PullRequest]): Unit = {
    val repo = new RepositoryId(owner, repoName)
    if (pr.isDefined) {
      Thread.sleep(1000); // giving github some time to check the PR that was just created
      val pullRequest = pr.get
      val prService = new PullRequestService(client)
      val status = prService.merge(repo, pullRequest.getNumber, commitMessage)
      if (!status.isMerged) {
        throw new Exception(s"Could not automatically merge ${pr.get.getTitle}: ${status.getMessage}")
      }
    }
  }

  def getFileContents(owner: String, repoName: String, relativePath: String): Option[String] = {
    val repo = new RepositoryId(owner, repoName)
    val contentsService = new ContentsService(client);
    val contents = contentsService.getContents(repo, relativePath)
    if (contents.size() > 0) {
      val encodedContents = contents.get(0).getContent
      Some(decode(encodedContents.replaceAll("\n", "").stripLineEnd))
    } else
      None
  }

  def getFileContents(url: String): String = {
    val gitUrl = GithubUrl(url)
    getFileContents(gitUrl.owner, gitUrl.repo, gitUrl.path).get
  }

  def getPullRequestById(owner: String, repoName: String, id: Int): Option[PullRequest] = {
    val repo = new RepositoryId(owner, repoName)
    val prService = new PullRequestService(client)
    val pullRequest = prService.getPullRequest(repo, id)
    Some(pullRequest)
  }

  /** Create pull request with specified contents.
    *
    * @param filesToAddOrModify Map of file paths to contents for the files to be committed to the branch
    * @return Pull request response
    */
  def createPullRequest(owner: String, repoName: String, filesToAddOrModify: Map[String, String], filesToDelete: List[String], titlePrefix: String, branchPrefix: String, userName: String, userEmail: String): Option[PullRequest] = {
    val repo = new RepositoryId(owner, repoName)

    val dataService = new DataService(client)

    // Get base head
    val baseRef = dataService.getReference(repo, s"heads/master")
    val baseSha1 = baseRef.getObject.getSha
    val baseCommit = dataService.getCommit(repo, baseSha1)

    // Push updated files to git
    val treeEntries: Array[TreeEntry] = new Array(filesToAddOrModify.size + filesToDelete.size)
    var i = 0
    for ((filePath, fileContents) <- filesToAddOrModify) {
      val blob = new Blob()
      blob.setContent(fileContents)
      blob.setEncoding(Blob.ENCODING_BASE64)
      val sha1 = dataService.createBlob(repo, blob)
      val treeEntry = new TreeEntry()
      treeEntry.setMode(TreeEntry.MODE_BLOB).setType(TreeEntry.TYPE_BLOB).setPath(filePath).setSha(sha1)
      treeEntries(i) = treeEntry
      i += 1
    }
    // Delete specified files
    for (fileToDelete <- filesToDelete) {
      val treeEntry = new TreeEntry()
      treeEntry.setMode(TreeEntry.MODE_BLOB).setType(TreeEntry.TYPE_BLOB).setPath(fileToDelete).setSha(null)
      treeEntries(i) = treeEntry
      i += 1
    }

    // Create commit point
    val tree = dataService.createTree(repo, asJavaCollection(treeEntries), baseSha1)
    val commitParams = new Commit
    val user = new CommitUser()
    val title = s"$titlePrefix generated from Simplan"
    user.setName(userName).setEmail(userEmail).setDate(Calendar.getInstance().getTime)
    commitParams.setMessage(title).setTree(tree).setAuthor(user).setCommitter(user).setParents(asList(baseCommit))
    val commit = dataService.createCommit(repo, commitParams)

    // Create new branch, and set head to the above commit
    val branchName = branchPrefix + Random.alphanumeric.take(8).mkString
    val refObject = new TypedResource()
    refObject.setSha(commit.getSha)
    val branchReference = new Reference()
    branchReference.setObject(refObject)
    branchReference.setRef(s"refs/heads/$branchName")
    dataService.createReference(repo, branchReference)

    // Create pull request
    val prHead = new PullRequestMarker
    prHead.setLabel(branchName)
    val prBase = new PullRequestMarker
    prBase.setLabel("master")
    val pullRequest = new PullRequest
    pullRequest.setTitle(title).setHead(prHead).setBase(prBase)
    val prService = new PullRequestService(client)
    Some(prService.createPullRequest(repo, pullRequest))
  }

  def getPullRequestStatus(pullRequest: PullRequest): Boolean = pullRequest.isMergeable

  def getPRForCommitSha(owner: String, repoName: String, sha: String): Option[String] = {
    val getPullsForCommit = s"""${scheme}://${host}/api/v3/repos/${owner}/${repoName}/commits/${sha}/pulls"""
    restClient.get[String](getPullsForCommit) match {
      case Success((pr, _)) => {
        val pullRequests = JacksonUtil.objectMapper.readValue(pr, new TypeReference[java.util.List[PullRequestModel]]() {});

        pullRequests.forEach(pullRequest => {
          if (pullRequest.getBase.getRef.equalsIgnoreCase("master"))
            return Option(pullRequest.getNumber)
        })
        None
      }
      case _ => throw new Exception("Unable to find PR number for the sha " + sha)
    }
  }

  def getFilesForPR(owner: String, repoName: String, prNo: Int): java.util.List[CommitFile] = {
    val repo = new RepositoryId(owner, repoName)
    val prService = new PullRequestService(client)
    prService.getFiles(repo, prNo)
  }

}

object GithubHandler extends Enumeration {
  type StatusState = Value
  final val mergeMessage = "Merged automatically for "

  val MaxStatusLen = 140
  val MaxCommentSize = 64000

  def apply(simplanConfiguration: SimplanAppContextConfiguration): GithubHandler = {
    val ghSystemConfig = simplanConfiguration.getSystemConfigAs[GithubSystemConfig]("github")
    val uri = new URL(ghSystemConfig.githubBaseUrl).toURI
    new GithubHandler(uri.getHost, ghSystemConfig.token.resolve, uri.getScheme, ghSystemConfig.port, ghSystemConfig.baseBranch)
  }

  def apply(baseUrl: String, token: String, baseBranch: String): GithubHandler = {
    val uri = new URL(baseUrl).toURI
    new GithubHandler(uri.getHost, token, uri.getScheme, uri.getPort, baseBranch)
  }
}
