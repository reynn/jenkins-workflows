# Python

## Overview

> Run Python scripts.

## Tools Section

| Name         | Required   | Type   | Default            | Description                                                               |
|:-------------|:-----------|:-------|:-------------------|:--------------------------------------------------------------------------|
| buildImage   | Required   | String |                    | Docker image containing Python and any non requirements.txt dependencies. |
| binary       | Optional   | String | `python`           | The Python binary to use, for example `python2` or `python3`.             |
| file         | Required   | String |                    | Path to a script to run, relative to the root of the project.             |
| requirements | Optional   | String | `requirements.txt` | Path to a requirements.txt file to install via Pip.                       |
| arguments    | Optional   | List   |                    | List of arguments to the script, should include any flags if needed.      |

## Available Methods

### script

> Run a Python script.

| Name         | Required   | Type   | Default            | Description                                                               |
|:-------------|:-----------|:-------|:-------------------|:--------------------------------------------------------------------------|
| buildImage   | Required   | String |                    | Docker image containing Python and any non requirements.txt dependencies. |
| binary       | Optional   | String | `python`           | The Python binary to use, for example `python2` or `python3`.             |
| file         | Required   | String |                    | Path to a script to run, relative to the root of the project.             |
| requirements | Optional   | String | `requirements.txt` | Path to a requirements.txt file to install via Pip.                       |
| arguments    | Optional   | List   |                    | List of arguments to the script, should include any flags if needed.      |

### script Example

```yaml
branches:
  feature:
    steps:
    - python:
      - script:
      - script:
          binary: python3
          script: scripts/update_docs.py
```

## Full Example Pipeline

```yaml
pipelines:
  tools:
    python:
      buildImage: "python:3.6-alpine3.7"
    branches:
      patterns:
        feature: .+
  branches:
    feature:
      steps:
        - python:
          - script:
              file: scripts/build.py
```

## Additional Resources

* [Official Docker images](https://hub.docker.com/_/python/)