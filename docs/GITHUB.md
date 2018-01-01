# Github

## Overview

> Steps for interacting with GitHub.

## Tools Section

| Name            | Default   | Section   | Type   |
|:----------------|:----------|:----------|:-------|
| patterns.master | `master`  | branches  | String |
| host            |           | github    | String |
| credentials     |           | github    | Map    |

## Available Methods

### createPullRequest

> Create a Pull Request in GitHub.

| Name        | Type   | Default                                                                       | Description                                                         |
|:------------|:-------|:------------------------------------------------------------------------------|:--------------------------------------------------------------------|
| fromBranch  | String | `<branch_name>`                                                               | The branch the PR will be merged from.                              |
| toBranch    | String | `master`                                                                      | The branch the PR will be merged into.                              |
| githubHost  | String | `determined by SCM config.`                                                   | The URL for the API for the GitHub instance.                        |
| credentials | Map    |                                                                               | Credentials to use when authenticating against the GitHub instance. |
| org         | String | `determined by SCM config.`                                                   | Organization/Owner of the repository.                               |
| repo        | String | `determined by SCM config.`                                                   | The name of the repo for the                                        |
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
  branches:
    feature:
      steps:
      - github:
        - createPullRequest:
  tools:
    branches:
      patterns:
        develop: develop
        feature: .+
        master: master
```

## Additional Resources

* [GitHub API](https://developer.github.com)