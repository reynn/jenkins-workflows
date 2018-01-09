# Nodejs

## Overview

> Execute any NPM, Grunt or Gulp task.

## Tools Section

| Name        | Type   | Default   | Section      | Description                                                   |
|:------------|:-------|:----------|:-------------|:--------------------------------------------------------------|
| dockerImage | String |           | nodejs       | Docker image to run all NodeJS commands in.                   |
| commandArgs | List   |           | nodejs.npm   | Additional arguments to the NPM commands.                     |
| command     | String | `install` | nodejs.npm   | The NPM command to run within a nodejs.npm workflow step.     |
| npmRegistry | String |           | nodejs.npm   | URL to an alternate NPM registry.                             |
| commandArgs | List   |           | nodejs.gulp  | Additional arguments to a Gulp command.                       |
| command     | String | `install` | nodejs.gulp  | The Gulp command to run within a nodejs.gulp workflow step.   |
| commandArgs | List   |           | nodejs.grunt | Additional arguments to a Grunt command.                      |
| command     | String | `install` | nodejs.grunt | The Grunt command to run within a nodejs.grunt workflow step. |

## Available Methods

### npm

### gulp

### grunt

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

* [NodeJS Official Site](https://nodejs.org/en/)
* [NPM](https://www.npmjs.com)
* [Grunt](https://gruntjs.com)
* [Gulp](https://gulpjs.com)
* [Docker Images](https://hub.docker.com/_/node/)