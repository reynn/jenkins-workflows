# Git

## Overview

> Various git functions for use within your pipeline.

## Tools Section

| Name        | Type    | Default                                | Section    | Description                                    |
|:------------|:--------|:---------------------------------------|:-----------|:-----------------------------------------------|
| message     | String  | `Automatic commit from {{ job_url }}`  | git.commit | The message to attach to the commit.           |
| pattern     | String  | `.`                                    | git.commit | Pattern for the `git add` command              |
| author      | String  | `${env.GIT_AUTHOR} <${env.GIT_EMAIL}>` | git.commit | Author of this commit                          |
| ammend      | Boolean | `False`                                | git.commit | Whether to ammend the previous commit.         |
| push        | Boolean | `True`                                 | git.commit | Push the commit to git as well.                |
| credentials | Map     |                                        | git.commit | Credentials to use when pushing to git origin. |

## Available Methods

### commit

> Execute an Ansible playbook

| Name        | Type    | Default                                | Section    | Description                                    |
|:------------|:--------|:---------------------------------------|:-----------|:-----------------------------------------------|
| message     | String  | `Automatic commit from {{ job_url }}`  | git.commit | The message to attach to the commit.           |
| pattern     | String  | `.`                                    | git.commit | Pattern for the `git add` command              |
| author      | String  | `${env.GIT_AUTHOR} <${env.GIT_EMAIL}>` | git.commit | Author of this commit                          |
| ammend      | Boolean | `False`                                | git.commit | Whether to ammend the previous commit.         |
| push        | Boolean | `True`                                 | git.commit | Push the commit to git as well.                |
| credentials | Map     |                                        | git.commit | Credentials to use when pushing to git origin. |

### commit Example

```yaml
branches:
  feature:
    steps:
    - git:
      - commit: Example email from {{ build_url }}
      - commit: null
```

## Full Example Pipeline

```yaml
pipelines:
  branches:
    feature:
      steps:
      - custom:
        - buildPackage: null
      - git:
        - commit:
            message: Automatic commit from pipeline.
  tools:
    branches:
      patterns:
        feature: .+
    git:
      credentials:
        description: Git credentials
```

## Additional Resources

* [Official documentation on commits](https://git-scm.com/docs/git-commit)