# Gradle

## Overview

> Execute any Gradle task.

## Tools Section

| Name       | Type   | Default     | Description                                                                            |
|:-----------|:-------|:------------|:---------------------------------------------------------------------------------------|
| buildImage | String |             | Docker image containing Gradle and any other necessary tools for the project to build. |
| binary     | String | `./gradlew` | The Gradle binary to use, typically this would be `gradlew` or `gradle`.               |
| task       | String | `build`     | The name of the task to execute, multiple tasks can be separated by a space.           |
| extraArgs  | List   |             | Any additional arguments to apply to the Gradle task.                                  |

## Available Methods

### task

> Execute Gradle tasks.

| Name       | Type   | Default     | Description                                                                            |
|:-----------|:-------|:------------|:---------------------------------------------------------------------------------------|
| buildImage | String |             | Docker image containing Gradle and any other necessary tools for the project to build. |
| binary     | String | `./gradlew` | The Gradle binary to use, typically this would be `gradlew` or `gradle`.               |
| name       | String | `build`     | The name of the task to execute, multiple tasks can be separated by a space.           |
| extraArgs  | List   |             | Any additional arguments to apply to the Gradle task.                                  |

### task Example

```yaml
branches:
  feature:
    steps:
    - gradle:
      - task:
      - task:
          binary: gradle
          name: compile
```

## Full Example Pipeline

```yaml
pipelines:
  tools:
    branches:
      patterns:
        feature: .+
  tools:
    gradle:
      buildImage: gradle:4.4-jdk9
  branches:
    feature:
      steps:
        - gradle:
          - task:
              binary: gradle
              task: "test build publish"
```

## Additional Resources

* [Gradle Official](https://gradle.org)
* [Gradle Docker images](https://hub.docker.com/_/gradle/)