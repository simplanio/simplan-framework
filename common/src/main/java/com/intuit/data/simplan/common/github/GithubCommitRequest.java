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

package com.intuit.data.simplan.common.github;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 09-Oct-2023 at 5:49 PM
 */
public class GithubCommitRequest implements Serializable {
    private String owner;
    private String repo;
    private String commitMessage;
    private String userName;
    private String userEmail;
    private String branch;
    private String baseBranch = "master";
    private Map<String, String> filesToAddOrModify;
    private List<String> filesToDelete;


    public String getOwner() {
        return owner;
    }

    public GithubCommitRequest setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public GithubCommitRequest setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public GithubCommitRequest setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public GithubCommitRequest setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        return this;
    }

    public Optional<String> getBranch() {
        return Optional.ofNullable(branch);
    }

    public String getRepo() {
        return repo;
    }

    public GithubCommitRequest setRepo(String repo) {
        this.repo = repo;
        return this;
    }

    public GithubCommitRequest setBranch(String branch) {
        this.branch = branch;
        return this;
    }

    public String getBaseBranch() {
        return baseBranch;
    }

    public GithubCommitRequest setBaseBranch(String baseBranch) {
        this.baseBranch = baseBranch;
        return this;
    }

    public Map<String, String> getFilesToAddOrModify() {
        return filesToAddOrModify;
    }

    public GithubCommitRequest setFilesToAddOrModify(Map<String, String> filesToAddOrModify) {
        this.filesToAddOrModify = filesToAddOrModify;
        return this;
    }

    public List<String> getFilesToDelete() {
        return filesToDelete;
    }

    public GithubCommitRequest setFilesToDelete(List<String> filesToDelete) {
        this.filesToDelete = filesToDelete;
        return this;
    }

    public GithubCommitRequest addToFilesToAddOrModify(String key, String value) {
        this.filesToAddOrModify.put(key, value);
        return this;
    }

    public GithubCommitRequest addToFilesToDelete(String value) {
        this.filesToDelete.add(value);
        return this;
    }
}
