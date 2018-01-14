import com.concur.*;

workflowDoc = '''
overview: "Various git functions for use within your pipeline. __NOTE__: You are fully responsible for usage of this workflow, there is potential to cause bad commits."
additional_resources:
  - name: Official documentation on commits
    url: https://git-scm.com/docs/git-commit
tools:
  - type: String
    section: git.commit
    name: message
    description: The message to attach to the commit.
    default: "Automatic commit from {{ job_url }}"
  - type: String
    section: git.commit
    name: pattern
    description: Pattern for the `git add` command.
    default: "."
  - type: String
    section: git.commit
    name: author
    description: Author of this commit.
    default: ${env.GIT_AUTHOR} <${env.GIT_EMAIL}>
  - type: Boolean
    section: git.commit
    name: amend
    description: Whether to amend the previous commit.
    default: false
  - type: Boolean
    section: git.commit
    name: push
    description: Push the commit to git as well.
    default: true
  - type: Map
    section: git.commit
    name: credentials
    description: Credentials to use when pushing to git origin.
full_example: |
  pipelines:
    tools:
      git:
        credentials:
          description: Git credentials
      branches:
        patterns:
          feature: .+
    branches:
      feature:
        steps:
          - custom: # This should be your build process
            - buildPackage:
          - git:
            - commit:
                message: Automatic commit from pipeline.
'''

concurPipeline  = new Commands()
concurGit       = new Git()
concurUtil      = new Util()

/*
description: Execute an Ansible playbook
parameters:
  - type: String
    name: message
    description: The message to attach to the commit.
    default: "Automatic commit from {{ job_url }}"
  - type: String
    name: pattern
    description: Pattern for the `git add` command.
    default: "."
  - type: String
    name: author
    description: Author of this commit in standard git format `Name <email@domain.com>`.
    default: ${env.GIT_AUTHOR} <${env.GIT_EMAIL}>
  - type: Boolean
    name: amend
    description: Whether to amend the previous commit.
    default: false
  - type: Boolean
    name: push
    description: Push the commit to git as well.
    default: true
  - type: Map
    name: credentials
    description: Credentials to use when pushing to git origin.
example:
  branches:
    feature:
      steps:
        - git:
            # Simple
            - commit: "Example email from {{ build_url }}"
            # Advanced
            - commit:
 */
public commit(Map yml, Map args) {
  String message      = args?.message     ?: yml.tools?.git?.commit?.message      ?: "Automatic commit from {{ build_number }}"
  String pattern      = args?.pattern     ?: yml.tools?.git?.commit?.pattern      ?: '.'
  String author       = args?.author      ?: yml.tools?.git?.commit?.author       ?: env.GIT_AUTHOR
  String email        = args?.email       ?: yml.tools?.git?.commit?.email        ?: env.GIT_EMAIL
  Map credentials     = args?.credentials ?: yml.tools?.git?.commit?.credentials  ?: yml.tools?.git?.credentials
  // False and null are false for groovys booleans, so checking if null on both params and tools section before defaulting.
  Boolean amend       = args?.amend       == null ? (yml.tools?.git?.commit?.amend       == null ? false : yml.tools?.git?.commit?.amend)       : args?.amend
  Boolean forceAdd    = args?.forceAdd    == null ? (yml.tools?.git?.commit?.forceAdd    == null ? false : yml.tools?.git?.commit?.forceAdd)    : args?.forceAdd
  Boolean forcePush   = args?.forcePush   == null ? (yml.tools?.git?.commit?.forcePush   == null ? false : yml.tools?.git?.commit?.forcePush)   : args?.forcePush
  Boolean push        = args?.push        == null ? (yml.tools?.git?.commit?.push        == null ? true  : yml.tools?.git?.commit?.push)        : args?.push
  Boolean failOnError = args?.failOnError == null ? (yml.tools?.git?.commit?.failOnError == null ? false : yml.tools?.git?.commit?.failOnError) : args?.failOnError

  String gitCmd = "git add $pattern && git commit"

  if (push) {
    def status = concurGit.runGitShellCommand('git status -s').trim().tokenize('\n')
    if (!status) {
      println "No working changes to commit, skipping."
      return
    }
    message = concurUtil.mustacheReplaceAll(message)
    author  = concurUtil.mustacheReplaceAll(author)
    email   = concurUtil.mustacheReplaceAll(email)
    def credential = concurPipeline.getCredentialsWithCriteria(credentials)
    switch (credential.getClass()) {
      case com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl:
        withCredentials([usernamePassword(credentialsId: credential.id, passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
          String httpsGitUrl = "https://${env.GIT_USERNAME}:${env.GIT_PASSWORD}@${env.GIT_HOST}/${env.GIT_OWNER}/${env.GIT_REPO}.git"
          gitCmd = """git config user.name '$author' \
              && git config user.email '$email' \
              && git config push.default simple \
              && git remote set-url origin "$httpsGitUrl" \
              && git add ${forceAdd ? '-f' : ''} $pattern \
              && git commit ${amend ? '--amend' : ''} -m \"$message\" \
              && git push ${forcePush ? '-f' : ''} origin HEAD:${env.BRANCH_NAME}"""
          concurPipeline.debugPrint('Workflows :: Git :: Commit', [
            'message'     : message,
            'pattern'     : pattern,
            'author'      : author,
            'email'       : email,
            'amend'       : amend,
            'forceAdd'    : forceAdd,
            'push'        : push,
            'credentials' : credentials
          ])
          def retCode = sh returnStatus: true, script: gitCmd
          if (failOnError && retCode > 0) {
            error('Failed to push changes to GitHub, please look above in the output to see what happened.')
          }
        }
        break
      case com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey:
        withCredentials([sshUserPrivateKey(credentialsId: credential.id, keyFileVariable: 'GIT_SSH_KEY_FILE', passphraseVariable: 'GIT_SSH_PASSPHRASE', usernameVariable: 'GIT_SSH_USERNAME')]) {
          String sshGitUrl = "git@${env.GIT_HOST}:${env.GIT_OWNER}/${env.GIT_REPO}.git"
          gitCmd = """git config user.name '$author' \
              && git config user.email '$email' \
              && git config push.default simple \
              && git remote set-url origin "$sshGitUrl" \
              && git add ${forceAdd ? '-f' : ''} $pattern \
              && git commit ${amend ? '--amend' : ''} -m \"$message\" \
              && GIT_SSH_COMMAND='ssh -i ${env.GIT_SSH_KEY_FILE}' git push ${forcePush ? '-f' : ''} origin HEAD:${env.BRANCH_NAME}"""
          concurPipeline.debugPrint('Workflows :: Git :: Commit', [
            'message'     : message,
            'pattern'     : pattern,
            'author'      : author,
            'email'       : email,
            'amend'       : amend,
            'forceAdd'    : forceAdd,
            'push'        : push,
            'credentials' : credentials
          ])
          def retCode = sh returnStatus: true, script: gitCmd
          if (failOnError && retCode > 0) {
            error('Failed to push changes to GitHub, please look above in the output to see what happened.')
          }
        }
        break
    }
  } else {
    gitCmd = concurUtil.mustacheReplaceAll(gitCmd)
    sh gitCmd
  }
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'commit':
      return 'git: commit'
  }
}

return this;
