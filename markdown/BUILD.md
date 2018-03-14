# Build

## Overview

> Includes workflows for running various language independent build tools.

## Tools Section

| Name        | Type   | Default   | Section   | Description                             |
|:------------|:-------|:----------|:----------|:----------------------------------------|
| buildImage  | String |           | mage      | Docker image that has Mage installed.   |
| target      | String |           | mage      | The mage target to execute.             |
| mageFileDir | String | `.`       | mage      | The directory containing your magefile. |

## Available Methods

### mage

> Execute mage targets.

| Name        | Type   | Default   | Description                             |
|:------------|:-------|:----------|:----------------------------------------|
| buildImage  | String |           | Docker image that has Mage installed.   |
| target      | String |           | The mage target to execute.             |
| mageFileDir | String | `.`       | The directory containing your magefile. |

### mage Example

```yaml
branches:
  feature:
    steps:
      - build:
          # Simple
          - mage:
          # Advanced
          - mage:
              target: Install
```

## Full Example Pipeline

```yaml
pipelines:
  tools:
    mage:
      buildImage: "quay.io/example/mage"
    branches:
      patterns:
        feature: .+
  branches:
    feature:
      steps:
        - build:
          - mage:
        - build:
          - mage:
              target: Docker
```

## Additional Resources

* [Magefile GitHub](https://github.com/magefile/mage)
* [Magefile Official Docs](https://magefile.org)