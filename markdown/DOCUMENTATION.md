# Documentation

## Overview

> Includes workflows for running various language independent documentation generator tools.

## Tools Section

| Name        | Type   | Default   | Section   | Description                             |
|:------------|:-------|:----------|:----------|:----------------------------------------|
| buildImage  | String |           | mage      | Docker image that has Mage installed.   |
| target      | String |           | mage      | The mage target to execute.             |
| mageFileDir | String | `.`       | mage      | The directory containing your magefile. |

## Available Methods

### mkdocs

> Generate documentation using mkdocs

| Name       | Type   | Default   | Description                                                                                   |
|:-----------|:-------|:----------|:----------------------------------------------------------------------------------------------|
| buildImage | String |           | Docker image that has mkdocs installed.                                                       |
| command    | String | `build`   | Which mkdocs command to use, serve will not work, supported commands are build and gh-deploy. |
| extraArgs  | List   |           | A list of extra arguments to append to the command.                                           |

### mkdocs Example

```yaml
branches:
  feature:
    steps:
      - build:
          # Simple
          - mkdocs:
          # Advanced
          - mkdocs:
              command: gh-deploy
```

## Full Example Pipeline

```yaml
pipelines:
  tools:
    mkdocs:
      buildImage: "quay.io/example/mkdocs"
    branches:
      patterns:
        feature: .+
  branches:
    feature:
      steps:
        - documentation:
          - mkdocs:
```

## Additional Resources

* [Mkdocs](http://www.mkdocs.org)