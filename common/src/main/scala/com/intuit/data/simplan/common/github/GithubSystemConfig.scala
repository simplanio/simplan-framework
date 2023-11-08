package com.intuit.data.simplan.common.github

import com.intuit.data.simplan.global.domain.QualifiedParam

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 31-Mar-2022 at 5:44 PM
  */
case class GithubSystemConfig(token: QualifiedParam, githubBaseUrl: String, githubApiBaseUrl: String, baseBranch: String, port: Int = -1)
