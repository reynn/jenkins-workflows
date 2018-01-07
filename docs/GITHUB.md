# Github

## Overview

> Steps for interacting with GitHub.

## Tools Section

| Name            | Type   | Default                     | Section          | Description                                                                                                           |
|:----------------|:-------|:----------------------------|:-----------------|:----------------------------------------------------------------------------------------------------------------------|
| patterns.master | String | `master`                    | branches         | The branch the PR will be merged into, also referred to as baseRef.                                                   |
| host            | String | `determined by SCM config.` | github           | The URL for the API for the GitHub instance.                                                                          |
| credentials     | Map    |                             | github           | Credentials to use when authenticating against the GitHub instance.                                                   |
| changelogFile   | String | `CHANGELOG.md`              | github.changelog | Name or path to your changelog file, typically this should be a file called CHANGELOG.md in the root of your project. |
| separator       | String | `##`                        | github.changelog | Should match what the header for your releases are, and it should be consistent for all releases.                     |

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

> Create a release with notes from a CHANGELOG.md

| Name          | Type    | Default                                     | Description                                                                                                           |
|:--------------|:--------|:--------------------------------------------|:----------------------------------------------------------------------------------------------------------------------|
| changelogFile | String  | `CHANGELOG.md`                              | Name or path to your changelog file, typically this should be a file called CHANGELOG.md in the root of your project. |
| separator     | String  | `##`                                        | Should match what the header for your releases are, and it should be consistent for all releases.                     |
| name          | String  | `Determined by last tag, prefixed with `v`` | The display name of the release in GitHub.                                                                            |
| tag           | String  | `Determined by last tag, prefixed with `v`` | The Git tag that will be created for this release.                                                                    |
| preRelease    | Boolean |                                             | Mark the release as a pre-release.                                                                                    |
| draft         | Boolean |                                             | Mark the release as a draft.                                                                                          |
| notes         | String  |                                             | Optionally provide the notes directly in pipelines.yml, not recommended.                                              |

### createRelease Example

```yaml
branches:
  feature:
    steps:
    - github:
      - createRelease:
      - createRelease:
          name: 0.1.0-alpha
          notes: '### New Features

            * Pipeline execution'
```

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
* [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)