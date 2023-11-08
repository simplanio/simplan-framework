package com.intuit.data.simplan.common.utils

import com.intuit.data.simplan.common.github.GithubCommitResponse
import org.eclipse.egit.github.core._
import org.eclipse.egit.github.core.service.DataService

import java.util.Arrays.asList
import java.util.{Base64, Calendar}
import scala.collection.JavaConverters.asJavaCollection
import scala.util.Random

object GithubUtils {

  def encode(contents: String): String = {
    new String(Base64.getEncoder.encode(contents.getBytes))
  }

  def decode(contents: String): String = {
    new String(Base64.getDecoder.decode(contents.getBytes))
  }

  def createTreeEntries(repo: RepositoryId, dataService: DataService, filesToAddOrModify: Map[String, String], filesToDelete: List[String]): List[TreeEntry] = {
    val upsertTreeEntry = filesToAddOrModify.map {
      case (filePath, fileContents) =>
        val blob = new Blob().setContent(fileContents) //.setEncoding(Blob.ENCODING_BASE64)
        val str = dataService.createBlob(repo, blob)
        new TreeEntry()
          .setMode(TreeEntry.MODE_BLOB)
          .setType(TreeEntry.TYPE_BLOB)
          .setPath(filePath)
          .setSha(str)
    }

    val deleteTreeEntry = filesToDelete.map(filePath =>
      new TreeEntry()
        .setMode(TreeEntry.MODE_BLOB)
        .setType(TreeEntry.TYPE_BLOB)
        .setPath(filePath)
        .setSha(null))
    val allEntries = upsertTreeEntry ++ deleteTreeEntry
    allEntries.toList
  }

  def commitToExistingBranch(dataService: DataService, fullCommitMessage: String, repo: RepositoryId, allEntries: List[TreeEntry], user: CommitUser, branchReference: Reference): GithubCommitResponse = {
    val baseSha1 = branchReference.getObject.getSha
    val tree = dataService.createTree(repo, asJavaCollection(allEntries), baseSha1)
    val baseCommit = dataService.getCommit(repo, baseSha1)
    val commitParams = new Commit().setMessage(fullCommitMessage).setTree(tree).setAuthor(user).setCommitter(user).setParents(asList(baseCommit))
    val commit = dataService.createCommit(repo, commitParams)
    val refObject: TypedResource = new TypedResource()
    refObject.setSha(commit.getSha)
    branchReference.setObject(refObject)
    dataService.editReference(repo, branchReference, true)
    new GithubCommitResponse().setSha(commit.getSha).setMessage(fullCommitMessage).setBranch(branchReference.getRef)
  }

  def createBranchAndCommit(
      dataService: DataService,
      branchName: Option[String],
      baseBranch: String,
      fullCommitMessage: String,
      repo: RepositoryId,
      allEntries: List[TreeEntry],
      user: CommitUser,
      branchPrefix: String): GithubCommitResponse = {

    val fullBranchName = branchName match {
      case Some(branchNameValue) => s"$branchPrefix-$branchNameValue"
      case None                  => s"$branchPrefix-${Random.alphanumeric.take(10).mkString}"
    }
    val baseRef = dataService.getReference(repo, s"refs/heads/$baseBranch")
    val tree = dataService.createTree(repo, asJavaCollection(allEntries), baseRef.getObject.getSha)

    val commitParams = new Commit().setMessage(fullCommitMessage).setTree(tree).setAuthor(user).setCommitter(user).setParents(asList(dataService.getCommit(repo, baseRef.getObject.getSha)))
    val commit = dataService.createCommit(repo, commitParams)

    val refObject: TypedResource = new TypedResource()
    refObject.setSha(commit.getSha)

    val branchReference: Reference = new Reference()
      .setObject(refObject)
      .setRef(s"refs/heads/$fullBranchName")
    dataService.createReference(repo, branchReference)
    new GithubCommitResponse().setSha(commit.getSha).setMessage(fullCommitMessage).setBranch(branchReference.getRef)
  }

  def createCommitUser(userName: String, userEmail: String): CommitUser =
    new CommitUser()
      .setName(userName)
      .setEmail(userEmail)
      .setDate(Calendar.getInstance().getTime)

}
