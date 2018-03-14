import com.concur.*;

workflowDoc = '''
title: GitHub
overview: Steps for interacting with GitHub.
additional_resources:
  - name: GitHub API
    url: https://developer.github.com
  - name: GitHub PR and Issue Templates
    url: https://github.com/blog/2111-issue-and-pull-request-templates
  - name: Keep a Changelog
    url: http://keepachangelog.com/en/1.0.0/
tools:
  - type: String
    name: patterns.master
    section: branches
    description: The branch the PR will be merged into, also referred to as baseRef.
    default: master
  - type: String
    name: host
    description: The URL for the API for the GitHub instance.
    default: determined by SCM config.
    section: github
  - type: Map
    name: credentials
    section: github
    description: Credentials to use when authenticating against the GitHub instance.
  - type: String
    name: changelogFile
    section: github.changelog
    default: 'CHANGELOG.md'
    description: Name or path to your changelog file, typically this should be a file called CHANGELOG.md in the root of your project.
  - type: String
    name: separator
    section: github.changelog
    default: '##'
    description: Should match what the header for your releases are, and it should be consistent for all releases.
full_example: |
  pipelines:
    tools:
      branches:
        patterns:
          master: master
          develop: develop
          feature: .+
    branches:
      feature:
        steps:
          - github:
            - createPullRequest:
'''

concurPipeline  = new Commands()
concurGit       = new Git()
concurGitHubApi = new GitHubApi()
concurUtil      = new Util()

/*
description: Create a Pull Request in GitHub.
parameters:
  - type: String
    name: fromBranch
    default: <branch_name>
    description: The branch the PR will be merged from, also referred to as headRef.
  - type: String
    name: toBranch
    default: master
    description: The branch the PR will be merged into, also referred to as baseRef.
  - type: String
    name: githubHost
    default: determined by SCM config.
    description: The URL for the API for the GitHub instance.
  - type: Map
    name: credentials
    description: Credentials to use when authenticating against the GitHub instance.
  - type: String
    name: org
    default: determined by SCM config.
    description: Organization/Owner of the repository.
  - type: String
    name: repo
    default: determined by SCM config.
    description: The name of the repo for to create PR for.
  - type: String
    name: title
    default: Merge {{ from_branch }} into {{ target_branch }}
    description: Name of the pull request.
  - type: String
    name: summary
    default: Created by Buildhub run {{ build_url }}. Will load a template if available.
    description: A brief summary of the pull request.
example: |
  branches:
    feature:
      steps:
        - github:
            # Simple
            - createPullRequest:
            # Advanced
            - createPullRequest:
                toBranch: develop
                title: Fix for issue {{ branch_name }}.
 */
public createPullRequest(Map yml, Map args) {
  Map gitData         = concurGit.getGitData()
  String fromBranch   = args?.fromBranch  ?: env.BRANCH_NAME
  String toBranch     = args?.toBranch    ?: yml.tools?.branches?.patterns?.master  ?: 'master'
  String githubHost   = args?.host        ?: yml.tools?.github?.host
  Map credentials     = args?.credentials ?: yml.tools?.github?.credentials
  String owner        = args?.owner       ?: gitData.owner
  String repo         = args?.repo        ?: gitData.repo
  String title        = args?.title       ?: "Merge {{ from_branch }} into {{ target_branch }}"
  String summary      = args?.summary     ?: "Created by job run {{ build_url }}."

  // env.CHANGE_FORK is set to the organization the fork is from and is only set on a PR build
  if (env.CHANGE_FORK) {
    concurPipeline.debugPrint('Workflow :: GitHub :: createPullRequest', 'Skipping pull request creation for forked repo.')
    return
  }

  Map replaceOptions  = ['from_branch': fromBranch, 'target_branch': toBranch]

  concurPipeline.debugPrint('Workflow :: GitHub :: createPullRequest', [
    'fromBranch'    : fromBranch,
    'toBranch'      : toBranch,
    'owner'         : owner,
    'repo'          : repo,
    'replaceOptions': replaceOptions,
    'title'         : title,
    'githubHost'    : githubHost,
    'summary'       : summary
  ])

  List prTemplates = findFiles glob: '.github/PULL_REQUEST_TEMPLATE*'

  if (prTemplates.size() > 0) {
    println "Pull Request template found, using that instead of provided summary..."
    String templateContents = readFile(prTemplates[0].toString())
    summary = concurUtil.mustacheReplaceAll(templateContents)
  }

  Map pullRequestResult = concurGitHubApi.createPullRequest(concurUtil.mustacheReplaceAll(title, replaceOptions),
                                                            fromBranch,
                                                            toBranch,
                                                            owner,
                                                            repo,
                                                            githubHost,
                                                            credentials,
                                                            concurUtil.mustacheReplaceAll(summary, replaceOptions))
  concurPipeline.debugPrint('Workflow :: GitHub :: createPullRequest', ['pullRequestResult': pullRequestResult])
  if (pullRequestResult instanceof List) {
    println "A pull request already existed and can be viewed at ${pullRequestResult[0].url}."
  }
}

/*
description: Create a release with notes from a CHANGELOG.md
parameters:
  - type: String
    name: changelogFile
    default: 'CHANGELOG.md'
    description: Name or path to your changelog file, typically this should be a file called CHANGELOG.md in the root of your project.
  - type: String
    name: separator
    default: '##'
    description: Should match what the header for your releases are, and it should be consistent for all releases.
  - type: String
    name: name
    default: Determined by last tag, prefixed with `v`
    description: The display name of the release in GitHub.
  - type: String
    name: tag
    default: Determined by last tag, prefixed with `v`
    description: The Git tag that will be created for this release.
  - type: Boolean
    name: preRelease
    description: Mark the release as a pre-release.
  - type: Boolean
    name: draft
    description: Mark the release as a draft.
  - type: String
    name: notes
    description: Optionally provide the notes directly in pipelines.yml, not recommended.
example: |
  branches:
    feature:
      steps:
        - github:
            # Simple
            - createRelease:
            # Advanced
            - createRelease:
                name: 0.1.0-alpha
                notes: |
                  ### New Features
                  * Pipeline execution
 */
public createRelease(Map yml, Map args) {
  String genVersion = concurGit.getVersion(yml).split('-')[0]
  genVersion = genVersion.startsWith('v') ? "$genVersion" : "v$genVersion"

  Map credentials         = args?.credentials   ?: yml.tools?.github?.credentials
  String changelogFile    = args?.changelogFile ?: yml.tools?.github?.changelog?.file      ?: 'CHANGELOG.md'
  String versionSeperator = args?.separator     ?: yml.tools?.github?.changelog?.separator ?: '##'
  String releaseName      = args?.name          ?: genVersion
  String tagName          = args?.tag           ?: genVersion
  String releaseNotes     = args?.notes

  Boolean preRelease      = args?.preRelease == null ? false : args?.preRelease
  Boolean draft           = args?.draft      == null ? false : args?.draft

  assert releaseName  : 'Workflows :: github :: createRelease :: [name] not provided as an argument to this step.'
  assert tagName      : 'Workflows :: github :: createRelease :: [tag] not provided as an argument to this step.'

  if (!releaseNotes) {
    Map changelogReleases = concurUtil.parseChangelog(changelogFile, versionSeperator)
    def thisRelease = changelogReleases.find { it =~ /^v?${releaseName.replace('v', '')}/ }

    assert thisRelease : "Workflows :: github :: createRelease :: Unable to find release $releaseName in the $changelogFile and no release notes are provided to the step."

    releaseNotes = thisRelease.value.trim()

    List rSplit = thisRelease.key.split()
    String rVersion = rSplit[0]
    if (rSplit.size() > 1) {
      rSplit.each {
        String element = it.toLowerCase()
        if (element.contains('prerelease') || element.contains('pre-release')) {
          preRelease = true
        }
        if (element.contains('draft')) {
          draft = true
        }
      }
    }
  }

  println """Creating release
  |---------------------------------
  ||Release    |${releaseName.center(20)}|
  ||PreRelease |${"preRelease".center(20)}|
  ||Draft      |${"draft".center(20)}|
  """.stripMargin()

  concurGitHubApi.createRelease(credentials, releaseNotes, tagName, releaseName, preRelease, draft)
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'createPullRequest':
      def fromBranch  = args?.fromBranch  ?: env.BRANCH_NAME
      def toBranch    = args?.toBranch    ?: yml.tools?.github?.master ?: 'master'
      return (fromBranch && toBranch) ? "github: create pr: $fromBranch -> $toBranch" : 'github: create pr'
    case 'createRelease':
      return 'github: create release'
  }
}

public tests(Map yml, Map args) {
  String workflowName = 'github'
  println "Testing $workflowName"
}

return this;
