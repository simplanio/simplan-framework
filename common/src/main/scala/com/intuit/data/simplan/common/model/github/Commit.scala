package com.intuit.data.simplan.common.model.github

/**
 * The representation for a commit.
 *
 * @param url      The commit url
 * @param message  The commit message
 * @param added    The files added on this commit
 * @param removed  The files removed on this commit
 * @param modified The files modified on this commit
 */
case class Commit(url: String, message: String, added: List[String], removed: List[String], modified: List[String])
