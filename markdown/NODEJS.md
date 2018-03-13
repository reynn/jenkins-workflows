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

> Execute NPM tasks.

| Name        | Type   | Default   | Description                                               |
|:------------|:-------|:----------|:----------------------------------------------------------|
| dockerImage | String |           | Docker image to run all NodeJS commands in.               |
| commandArgs | List   |           | Additional arguments to the NPM commands.                 |
| command     | String | `install` | The NPM command to run within a nodejs.npm workflow step. |
| npmRegistry | String |           | URL to an alternate NPM registry.                         |

### npm Example

```yaml
branches:
  feature:
    steps:
    - nodejs:
      - node:
      - node:
          command: compile
```

### gulp

> Execute Gulp tasks.

| Name        | Type   | Default   | Description                                                 |
|:------------|:-------|:----------|:------------------------------------------------------------|
| dockerImage | String |           | Docker image to run all NodeJS commands in.                 |
| commandArgs | List   |           | Additional arguments to a Gulp command.                     |
| command     | String | `install` | The Gulp command to run within a nodejs.gulp workflow step. |

### gulp Example

```yaml
branches:
  feature:
    steps:
    - nodejs:
      - gulp:
      - gulp:
          name: compileScss
```

### grunt

> Execute Grunt tasks.

| Name        | Type   | Default   | Description                                                   |
|:------------|:-------|:----------|:--------------------------------------------------------------|
| dockerImage | String |           | Docker image to run all NodeJS commands in.                   |
| commandArgs | List   |           | Additional arguments to a Grunt command.                      |
| command     | String | `install` | The Grunt command to run within a nodejs.grunt workflow step. |

### grunt Example

```yaml
branches:
  feature:
    steps:
    - nodejs:
      - grunt:
      - grunt:
          name: webpack
```

## Full Example Pipeline

```yaml
pipelines:
  tools:
    branches:
      patterns:
        feature: .+
  tools:
    nodejs:
      buildImage: node:9.3-alpine
  branches:
    feature:
      steps:
        - nodejs:
          - npm:
        - docker:
          - build:
          - push:
```

## Additional Resources

* [NodeJS Official Site](https://nodejs.org/en/)
* [NPM](https://www.npmjs.com)
* [Grunt](https://gruntjs.com)
* [Gulp](https://gulpjs.com)
* [Docker Images](https://hub.docker.com/_/node/)