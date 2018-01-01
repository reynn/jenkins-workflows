# Github

## Overview

> Steps for interacting with GitHub.

## Tools Section

| Name            | Type   | Default                     | Section   | Description                                                         |
|:----------------|:-------|:----------------------------|:----------|:--------------------------------------------------------------------|
| patterns.master | String | `master`                    | branches  | The branch the PR will be merged into, also referred to as baseRef. |
| host            | String | `determined by SCM config.` | github    | The URL for the API for the GitHub instance.                        |
| credentials     | Map    |                             | github    | Credentials to use when authenticating against the GitHub instance. |

## Available Methods

### createPullRequest

> Create a Pull Request in GitHub.

| Name        | Type   | Default                                                                       | Description                                                         |
|:------------|:-------|:------------------------------------------------------------------------------|:--------------------------------------------------------------------|
| fromBranch  | String | `<branch_name>`                                                               | The branch the PR will be merged from, also referred to as headRef. |
| toBranch    | String | `master`                                                                      | The branch the PR will be merged into, also referred to as baseRef. |
| githubHost  | String | `determined by SCM config.`                                                   | The URL for the API for the GitHub instance.                        |
| credentials | Map    |                                                                               | Credentials to use when authenticating against the GitHub instance. |
| org         | String | `determined by SCM config.`                                                   | Organization/Owner of the repository.                               |
| repo        | String | `determined by SCM config.`                                                   | The name of the repo for to create PR for.                          |
| title       | String | `Merge {{ from_branch }} into {{ target_branch }}`                            | Name of the pull request.                                           |
| summary     | String | `Created by Buildhub run {{ build_url }}. Will load a template if available.` | A brief summary of the pull request.                                |

### createPullRequest Example

```yaml
branches:
  feature:
    steps:
    - github:
      - createPullRequest:
      - createPullRequest:
          title: Fix for issue {{ branch_name }}.
          toBranch: develop
```

### createRelease

## Full Example Pipeline

```yaml
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
```

## Additional Resources

* [GitHub API](https://developer.github.com)
* [GitHub PR and Issue Templates](https://github.com/blog/2111-issue-and-pull-request-templates)