@Library(value='ibp-libraries', changelog=false) _
@Library(value = 'msaas-shared-lib', changelog = false) l2

def config = [:]

// Commit hash
def commitId = ""
// Defining version for artifacts when it is a snapshot being built or release
def revisionNo = ""
// Converting Slashes to hyphens from branch name
def branchNameConverted = env.BRANCH_NAME.replace('/','-')
// Adding version suffix to a non-release build.
def versionSuffix = ('main' != env.BRANCH_NAME) ? "-${branchNameConverted}-SNAPSHOT" : ''
// Slack channel. For snapshots has a -snapshot appended
def slackChannel = "build-simplan"
def slackCrChannel = "${slackChannel}-cr-notifications"
slackChannel = ('main' == env.BRANCH_NAME) ? slackChannel : "${slackChannel}-snapshot"
// Slack messages for snapshot and release, please look below set in INIT DEFINITIONS to modify RELEASE MESSAGE value.
def slackRepoName = "*FRAMEWORK (OPEN SOURCE)*\n"
def slackSnapshotMessage  = "#<${env.RUN_DISPLAY_URL}|${env.BUILD_NUMBER}> for branch ${env.BRANCH_NAME}.\n"
def slackReleaseMessage  = ''
def slackMessage = slackRepoName + "#<${env.RUN_DISPLAY_URL}|${env.BUILD_NUMBER}>. "



pipeline {
  agent {
    kubernetes {
      defaultContainer "maven"
      yaml """
        apiVersion: v1
        kind: Pod
        metadata:
          annotations:
            iam.amazonaws.com/role: arn:aws:iam::733536204770:role/tech-ea-prd-jenkins
        spec:
          containers:
          - name: maven
            image: 'docker.artifactory.a.intuit.com/maven:3.5.3-jdk-8'
            tty: true
            command:
            - cat
            volumeMounts:
              - name: shared-build-output
                mountPath: /var/run/outputs
          - name: toolbox
            image: docker.intuit.com/data/kgpt/curation/service/jenkins-toolbox:latest
            tty: true
            command:
            - cat
            volumeMounts:
              - name: shared-build-output
                mountPath: /var/run/outputs
          - name: mkdocs
            image: docker.intuit.com/dev-test/oid-mkdocs-builder/service/oid-mkdocs-builder:latest
            tty: true
            command:
            - cat
            volumeMounts:
              - name: shared-build-output
                mountPath: /var/run/outputs
          - name: servicenow
            image: docker.intuit.com/coe/servicenow-cr-agent/service/servicenow-cr-agent:latest
            tty: true
            command: ["cat"]
            imagePullPolicy: Always
            volumeMounts:
              - name: shared-build-output
                mountPath: /var/run/outputs
          volumes:
            - name: shared-build-output
              emptyDir: {}
      """
    }
  }

  options {
    timestamps()
    ansiColor('xterm')
    /*
      daysToKeepStr: history is only kept up to this days.
      numToKeepStr: only this number of build logs are kept.
      artifactDaysToKeepStr: artifacts are only kept up to this days.
      artifactNumToKeepStr: only this number of builds have their artifacts kept.
    */
    buildDiscarder(logRotator(daysToKeepStr:'', numToKeepStr: numbersAccordingBranch(), artifactDaysToKeepStr: '', artifactNumToKeepStr: ''))
  }

  environment {
    IBP_MAVEN_SETTINGS_FILE = credentials("ibp-maven-settings-file")
    MAVEN_ARTIFACTORY_CREDENTIALS = credentials("artifactory-simplan-framework")
    MAVEN_ARTIFACTORY_USERID = "${env.MAVEN_ARTIFACTORY_CREDENTIALS_USR}"
    MAVEN_ARTIFACTORY_TOKEN = "${env.MAVEN_ARTIFACTORY_CREDENTIALS_PSW}"
    GIT_URL = "github.intuit.com/Simplan/simplan-framework.git"
    GIT_BRANCH = "${env.BRANCH_NAME}"
    ARTIFACT_VERSION = removeSpacesEnv(params.artifactVersion)
    S3_BUCKET_ROLE = 'arn:aws:iam::733536204770:role/tech-ea-prd-jenkins'
  }

  stages {
    stage ('INIT DEFINITIONS'){
      steps {
        script {
          config = readConfigYAML()
          commitId = sh(script: 'git rev-parse HEAD',returnStdout: true)
          revisionNo = ('main' == env.BRANCH_NAME) ? config.artifactVersion+'.'+env.BUILD_NUMBER : "1.0.0${versionSuffix}"
          slackReleaseMessage = "*RELEASE* v<${env.RUN_DISPLAY_URL}|${revisionNo}>\n"
          slackMessage = slackRepoName + (('main' == env.BRANCH_NAME) ? slackReleaseMessage : slackSnapshotMessage)
        }
      }
    }

    stage('BUILD CHECK. PRs & GENERAL BRANCHES') {
      when {
        anyOf {
          changeRequest ()
          not { branch 'main' }
        }
      }
      steps {
        mavenBuildPR("-U -B -s settings.xml")
      }
      post {
        success {
          PRPostSuccess(config)
        }
        always {
          PRPostAlways(config)
        }
      }
    }

    stage('RELEASE STAGE') {
      when {
        allOf {
          not { changeRequest() }
          branch 'main'
        }
      }
      stages {
		    stage('Create CR') {
          steps {
						slackCRNotification(slackRepoName, slackCrChannel)
            scorecardProdReadiness(config, 'prd')
            container('servicenow') {
              sh label: 'Opens CR', script: 'echo "Opens CR"'
              openSnowCR(config, 'prd', config.artifactId)
            }
          }
        }

        stage('BUILDING RELEASE'){
		      steps {
		        sh """
		          echo 'Creating file with versions used.'
		          printf 'simplan.system.ci.framework.version=${revisionNo}\nsimplan.system.ci.framework.commitHash=${commitId}' > global/src/main/resources/simplan-framework-manifest.conf
		        """
		        mavenBuildCI("-P upload-artifact -Drevision=${revisionNo} -U -B -s settings.xml")
		      //  gitTag(revisionNo, env.GIT_URL)
		      }
		      post {
		        success {
		          CIPostSuccess(config)
		        }
		        always {
		          script {
		            stage("Close CR"){
				          container('servicenow') {
		                sh label: 'Close CR', script: 'echo "Closes CR"'
		                closeSnowCR(config, 'prd')
		              }
	              }
		          }
		          CIPostAlways(config)
		        }
		      }
	      }

//		    stage('DOCUMENTATION DEPLOYMENT') {
//		      steps{
//		        container('mkdocs'){
//		          mkdocsFunction()
//		        }
//		      }
//		    }

	    }
    }
  }

  post {
    success {
      slackSend(channel: slackChannel, tokenCredentialId: 'slack-token-curation-tf', message: ":white_check_mark: " + slackMessage + "Build was SUCCESSFUL.")
    }
    failure {
      slackSend(channel: slackChannel, tokenCredentialId: 'slack-token-curation-tf', message: ":x: " + slackMessage + "Build FAILED.")
    }
    aborted {
      slackSend(channel: slackChannel, tokenCredentialId: 'slack-token-curation-tf', message: ":white_circle: " + slackMessage + "Build was ABORTED.")
    }
    always {
      customReleaseMetrics(config)
    }
  }
}

def readConfigYAML(){
  def values = readYaml file: "library-config.yaml"
  return values
}

def removeSpacesEnv(String word){
  word = (word?.trim()) ? word.replace(' ', '') : ""
  return word
}

def gitTag(String tagValue, String GIT_URL){
  withCredentials([usernamePassword(credentialsId: 'github-svc-sbseg-ci', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
    sh """
      set +x
      git config user.name svc-sbseg-ci
      git config user.email SBSEGCI-Admins@intuit.com
      git tag ${tagValue}
      git push https://${GIT_USERNAME}:${GIT_PASSWORD}@${GIT_URL} --tags
      set -x
    """
  }
}

def numbersAccordingBranch(){
  if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'develop') return '20'
  else return '5'
}

def mkdocsFunction(){
  withCredentials([usernamePassword(credentialsId: 'github-svc-sbseg-ci', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
    sh """
      git config user.name svc-sbseg-ci
      git config user.email SBSEGCI-Admins@intuit.com
      git config --global url."https://${GIT_USERNAME}:${GIT_PASSWORD}@github.intuit.com".insteadOf https://github.intuit.com
      git config remote.origin.fetch "+refs/heads/*:refs/remotes/origin/*"
      bash gh-deploy.sh
    """
  }
}

def slackCRNotification(slackRepoName, slackCrChannel){
	def message = "<!here>: ${slackRepoName} SNOW CR approval needed: <${env.RUN_DISPLAY_URL}|CLICK HERE>"
	slackSend(channel: slackCrChannel, tokenCredentialId: 'slack-token-curation-tf', message: message)
}
