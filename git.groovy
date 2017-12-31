import com.concur.*;

workflowDoc = '''
overview: Various git functions for use within your pipeline.
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
    description: Pattern for the `git add` command
    default: "."
  - type: String
    section: git.commit
    name: author
    description: Author of this commit
    default: ${env.GIT_AUTHOR} <${env.GIT_EMAIL}>
  - type: Boolean
    section: git.commit
    name: ammend
    description: Whether to ammend the previous commit.
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
full_example:
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
concurUtil      = new Util()

/*
description: Execute an Ansible playbook
parameters:
  - type: String
    section: git.commit
    name: message
    description: The message to attach to the commit.
    default: "Automatic commit from {{ job_url }}"
  - type: String
    section: git.commit
    name: pattern
    description: Pattern for the `git add` command
    default: "."
  - type: String
    section: git.commit
    name: author
    description: Author of this commit
    default: ${env.GIT_AUTHOR} <${env.GIT_EMAIL}>
  - type: Boolean
    section: git.commit
    name: ammend
    description: Whether to ammend the previous commit.
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
  String message  = args?.message     ?: yml.tools?.git?.commit?.message      ?: "Automatic commit from {{ build_number }}"
  String pattern  = args?.pattern     ?: yml.tools?.git?.commit?.pattern      ?: '.'
  String author   = args?.author      ?: yml.tools?.git?.commit?.author       ?: env.GIT_AUTHOR
  String email    = args?.email       ?: yml.tools?.git?.commit?.email        ?: env.GIT_EMAIL
  Boolean ammend  = args?.ammend      ?: yml.tools?.git?.commit?.ammend       ?: false
  Boolean force   = args?.force       ?: yml.tools?.git?.commit?.force        ?: false
  Boolean push    = args?.push        ?: yml.tools?.git?.commit?.push         ?: true
  Map credentials = args?.credentials ?: yml.tools?.git?.commit?.credentials

  String gitCmd = "git add $pattern && git commit"

  if (push) {
    message = concurUtil.mustacheReplaceAll(message)
    author  = concurUtil.mustacheReplaceAll(author)
    email   = concurUtil.mustacheReplaceAll(email)
    def credential = concurPipeline.getCredentialsWithCriteria(credentials)
    switch (credential.getClass()) {
      case com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl:
        withCredentials([usernamePassword(credentialsId: credential.id, passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
          sh """git config user.name '$author'
              |&& git config user.email '$email'
              |&& git config push.default simple
              |&& git remote set-url origin "https://${env.GIT_USERNAME}:${env.GIT_PASSWORD}@${env.GIT_HOST}/${env.GIT_ORG}/${env.GIT_REPO}.git"
              |&& git add ${force ? '-f' : ''} $pattern
              |&& git commit ${ammend ? '--ammend' : ''} -m \"$message\"
              |&& git push origin HEAD:${env.BRANCH_NAME}""".stripMargin()
        }
        break
      case com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey:
        withCredentials([sshUserPrivateKey(credentialsId: credential.id, keyFileVariable: 'GIT_SSH_KEY_FILE', passphraseVariable: 'GIT_SSH_PASSPHRASE', usernameVariable: 'GIT_SSH_USERNAME')]) {
          sh """git config user.name '$author'
              |&& git config user.email '$email'
              |&& git config push.default simple
              |&& git remote set-url origin "git@${env.GIT_HOST}:${env.GIT_ORG}/${env.GIT_REPO}.git"
              |&& git add ${force ? '-f' : ''} $pattern
              |&& git commit ${ammend ? '--ammend' : ''} -m \"$message\"
              |&& GIT_SSH_COMMAND='ssh -i ${env.GIT_SSH_KEY_FILE}' git push origin HEAD:${env.BRANCH_NAME}""".stripMargin()
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
