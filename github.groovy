import com.concur.*;

workflowDoc = '''
overview: Steps for interacting with GitHub.
additional_resources:
  - name: GitHub API
    url: https://developer.github.com
tools:
  - type: String
    name: patterns.master
    section: branches
    default: master
  - type: String
    name: host
    section: github
  - type: Map
    name: credentials
    section: github
full_example:
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
    description: The branch the PR will be merged from.
  - type: String
    name: toBranch
    default: master
    description: The branch the PR will be merged into.
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
    description: The name of the repo for the 
  - type: String
    name: title
    default: Merge {{ from_branch }} into {{ target_branch }}
    description: Name of the pull request.
  - type: String
    name: summary
    default: Created by Buildhub run {{ build_url }}. Will load a template if available.
    description: A brief summary of the pull request.
example:
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
  String org          = args?.org         ?: gitData.org
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
    'org'           : org,
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
                                                            org,
                                                            repo,
                                                            githubHost,
                                                            credentials,
                                                            concurUtil.mustacheReplaceAll(summary, replaceOptions))
  concurPipeline.debugPrint('Workflow :: GitHub :: createPullRequest', ['pullRequestResult': pullRequestResult])
  if (pullRequestResult instanceof List) {
    println "A pull request already existed and can be viewed at ${pullRequestResult[0].url}."
  }
}

public createRelease(Map yml, Map args) {

}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'createPullRequest':
      def fromBranch  = args?.fromBranch  ?: env.BRANCH_NAME
      def toBranch    = args?.toBranch    ?: yml.tools?.github?.master ?: 'master'
      return (fromBranch && toBranch) ? "github: createPR: $fromBranch -> $toBranch" : 'github: createPR'
    case 'createRelease':
      return 'github: create release'
  }
}

return this;
