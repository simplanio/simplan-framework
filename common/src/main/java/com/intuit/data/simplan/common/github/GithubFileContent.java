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

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 09-Oct-2023 at 4:30 PM
 */
public class GithubFileContent {

    String owner;
    String repo;
    String path;
    String content;
    String branch;
    String sha;

    String message;

    public String getMessage() {
        return message;
    }

    public GithubFileContent setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getSha() {
        return sha;
    }

    public GithubFileContent setSha(String sha) {
        this.sha = sha;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public GithubFileContent setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getRepo() {
        return repo;
    }

    public GithubFileContent setRepo(String repo) {
        this.repo = repo;
        return this;
    }

    public String getPath() {
        return path;
    }

    public GithubFileContent setPath(String path) {
        this.path = path;
        return this;
    }

    public String getContent() {
        return content;
    }

    public GithubFileContent setContent(String content) {
        this.content = content;
        return this;
    }

    public String getBranch() {
        return branch;
    }

    public GithubFileContent setBranch(String branch) {
        this.branch = branch;
        return this;
    }
}
