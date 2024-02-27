package com.intuit.data.simplan.common.github

import com.google.gson.reflect.TypeToken
import org.eclipse.egit.github.core.{IRepositoryIdProvider, RepositoryCommit}
import org.eclipse.egit.github.core.client.{GitHubClient, PageIterator, PagedRequest}
import org.eclipse.egit.github.core.service.CommitService

import java.util
import java.util.List

class CommitServiceExtention(client: GitHubClient) extends CommitService {

  def pageCommits(repository: IRepositoryIdProvider, sha: String, path: String, pageSize: Int, pageNumber: Int): PageIterator[RepositoryCommit] = {
    val id: String = this.getId(repository)
    val uri: StringBuilder = new StringBuilder("/repos")
    uri.append('/').append(id)
    uri.append("/commits")
    val request: PagedRequest[RepositoryCommit] = this.createPagedRequest(pageNumber, pageSize)
    request.setUri(uri.toString())
    request.setType(new TypeToken[List[RepositoryCommit]]() {}.getType)
    if (sha != null || path != null) {
      val params: util.Map[String, String] = new util.HashMap[String, String]()
      if (sha != null) params.put("sha", sha)
      if (path != null) params.put("path", path)
      request.setParams(params)
    }

    this.createPageIterator(request)

  }

}
