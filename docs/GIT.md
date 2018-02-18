# Git

## Overview

> Various git functions for use within your pipeline. __NOTE__: You are fully responsible for usage of this workflow, there is potential to cause bad commits.

## Tools Section

| Name        | Type    | Default                                | Section    | Description                                    |
|:------------|:--------|:---------------------------------------|:-----------|:-----------------------------------------------|
| message     | String  | `Automatic commit from {{ job_url }}`  | git.commit | The message to attach to the commit.           |
| pattern     | String  | `.`                                    | git.commit | Pattern for the `git add` command.             |
| author      | String  | `${env.GIT_AUTHOR} <${env.GIT_EMAIL}>` | git.commit | Author of this commit.                         |
| amend       | Boolean | `False`                                | git.commit | Whether to amend the previous commit.          |
| push        | Boolean | `True`                                 | git.commit | Push the commit to git as well.                |
| credentials | Map     |                                        | git.commit | Credentials to use when pushing to git origin. |

## Available Methods

### commit

> Execute an Ansible playbook

| Name        | Type    | Default                                | Description                                                             |
|:------------|:--------|:---------------------------------------|:------------------------------------------------------------------------|
| message     | String  | `Automatic commit from {{ job_url }}`  | The message to attach to the commit.                                    |
| pattern     | String  | `.`                                    | Pattern for the `git add` command.                                      |
| author      | String  | `${env.GIT_AUTHOR} <${env.GIT_EMAIL}>` | Author of this commit in standard git format `Name <email@domain.com>`. |
| amend       | Boolean | `False`                                | Whether to amend the previous commit.                                   |
| push        | Boolean | `True`                                 | Push the commit to git as well.                                         |
| credentials | Map     |                                        | Credentials to use when pushing to git origin.                          |

### commit Example

```yaml
branches:
  feature:
    steps:
    - git:
      - commit: Example email from {{ build_url }}
      - commit:
```

### tests

## Full Example Pipeline

```yaml
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
```

## Additional Resources

* [Official documentation on commits](https://git-scm.com/docs/git-commit)