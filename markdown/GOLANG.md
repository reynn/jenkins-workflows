# Golang

## Overview

> Steps for building and testing with Golang.

## Tools Section

| Name           | Required   | Type    | Default                | Section   | Description                                                                                                                    |
|:---------------|:-----------|:--------|:-----------------------|:----------|:-------------------------------------------------------------------------------------------------------------------------------|
| buildImage     | Required   | String  |                        | golang    | Docker image that has Golang/Glide/Godep installed.                                                                            |
| additionalArgs |            | List    |                        | glide     | Any additional arguments to Glide as a YAML style List.                                                                        |
| command        |            | String  | `install`              | glide     | Which Glide command to run.                                                                                                    |
| additionalArgs |            | List    |                        | godep     | Any additional arguments to Godep as a YAML style List.                                                                        |
| command        |            | String  | `restore`              | godep     | Which Godep command to run.                                                                                                    |
| goPath         |            | String  | `determined by SCM`    | golang    | The path within the container to mount the project into.                                                                       |
| outFile        |            | String  |                        | golang    | Where the built Go binary will be put instead of the current directory.                                                        |
| env            |            | Map     |                        | golang    | Setup for the build environment, for example setting GOOS or GOARCH.                                                           |
| additionalArgs |            | List    |                        | golang    | Any additional arguments to `go build` as a YAML style List.                                                                   |
| mainPath       |            | String  |                        | golang    | Path to the main .go file to build.                                                                                            |
| additionalArgs |            | List    |                        | test      | Additional arguments to the test binary specified.                                                                             |
| binary         |            | String  | `go test`              | test      | The binary to use for the test, in case a different framework is being used.                                                   |
| resultsPath    |            | String  | `test_results`         | test      | If a test framework, such as Gingko, that can output to Junit is being used this is the path to the directory.                 |
| gatherJunit    |            | Boolean | `False`                | golang    | If a test framework, such as Gingko, that can output to Junit this will ensure that the test results are published in Jenkins. |
| junitPattern   |            | String  | `${resultsPath}/*.xml` | golang    | An ant style pattern for the junit plugin, should match where your test results get stored.                                    |

## Available Methods

### glide

> Vendor Package Management for your Go projects.

| Name           | Required   | Type   | Default             | Description                                              |
|:---------------|:-----------|:-------|:--------------------|:---------------------------------------------------------|
| buildImage     | Required   | String |                     | Docker image that has Glide installed.                   |
| additionalArgs |            | List   |                     | Any additional arguments to Glide as a YAML style List.  |
| command        |            | String | `install`           | Which Glide command to run.                              |
| goPath         |            | String | `determined by SCM` | The path within the container to mount the project into. |

### glide Example

```yaml
branches:
  feature:
    steps:
    - golang:
      - glide:
      - glide:
          additionalArgs:
          - --force
          command: install
```

### dep

> Dep is a tool for managing Go package dependencies.

| Name           | Required   | Type   | Default             | Description                                              |
|:---------------|:-----------|:-------|:--------------------|:---------------------------------------------------------|
| buildImage     | Required   | String |                     | Docker image that has Godep installed.                   |
| additionalArgs |            | List   |                     | Any additional arguments to Godep as a YAML style List.  |
| command        |            | String | `restore`           | Which Godep command to run.                              |
| goPath         |            | String | `determined by SCM` | The path within the container to mount the project into. |

### dep Example

```yaml
branches:
  feature:
    steps:
    - golang:
      - dep:
      - dep:
          additionalArgs:
          - -v
          - -update
```

### lint

> Build a Golang project.

| Name            | Type   | Default        | Description                                                        |
|:----------------|:-------|:---------------|:-------------------------------------------------------------------|
| buildImage      | String |                | Docker image that has the linting tool installed.                  |
| additionalFlags | List   |                | Any additional arguments to the linting tool as a YAML style List. |
| enable          | List   | `[]`           | A list of linters to enable.                                       |
| binary          | String | `gometalinter` | The binary you want to use for linting.                            |
| goPath          | String | `getGoPath()`  | The path within the container to mount the project into.           |

### lint Example

```yaml
branches:
  feature:
    steps:
    - golang:
      - lint:
      - lint:
          additionalFlags:
          - tests
          binary: gometalinter.v1
          enable:
          - vet
          - deadcode
          - goconst
          - errcheck
          - goimports
```

### build

> Build a Golang project.

| Name       | Required   | Type   | Default             | Section   | Description                                                             |
|:-----------|:-----------|:-------|:--------------------|:----------|:------------------------------------------------------------------------|
| buildImage | Required   | String |                     |           | Docker image that has any Golang installed.                             |
| goPath     |            | String | `determined by SCM` |           | The path within the container to mount the project into.                |
| outFile    |            | String |                     | golang    | Where the built Go binary will be put instead of the current directory. |
| env        |            | Map    |                     | golang    | Setup for the build environment, for example setting GOOS or GOARCH.    |
| mainPath   |            | String |                     | golang    | Path to the main .go file to build.                                     |

### build Example

```yaml
branches:
  feature:
    steps:
    - golang:
      - build:
      - build:
          env:
            GOARCH: amd64
            GOOS: linux
          mainPath: cmd/app/main.go
          outFile: publish/example-binary
```

### test

> Build a Golang project.

| Name           | Required   | Type    | Default                | Description                                                                                                                    |
|:---------------|:-----------|:--------|:-----------------------|:-------------------------------------------------------------------------------------------------------------------------------|
| buildImage     | Required   | String  |                        | Docker image that has any Golang installed.                                                                                    |
| goPath         |            | String  | `determined by SCM`    | The path within the container to mount the project into.                                                                       |
| additionalArgs |            | List    |                        | Any additional arguments to Glide as a YAML style List.                                                                        |
| binary         |            | String  | `go test`              | The binary to use for the test, in case a different framework is being used.                                                   |
| resultsPath    |            | String  | `test_results`         | If a test framework, such as Gingko, that can output to Junit is being used this is the path to the directory.                 |
| gatherJunit    |            | Boolean | `False`                | If a test framework, such as Gingko, that can output to Junit this will ensure that the test results are published in Jenkins. |
| junitPattern   |            | String  | `${resultsPath}/*.xml` | An ant style pattern for the junit plugin, should match where your test results get stored.                                    |

### test Example

```yaml
branches:
  feature:
    steps:
    - golang:
      - test:
      - test:
          additionalArgs:
          - ./...
          binary: ginkgo
          gatherJunit: true
          resultsPath: results
```

## Full Example Pipeline

```yaml
pipelines:
  tools:
    branches:
      patterns:
        feature: .+
  branches:
    feature:
      steps:
        - golang:
          - glide:
          - test:
              binary: ginkgo
              gatherJunit: true
          - build:
              mainPath: cmd/app/main.go
              outFile: publish/app
              env:
                GOOS: darwin
                GOARCH: amd64
```

## Additional Resources

* [Glide](https://github.com/Masterminds/glide)
* [Dep](https://github.com/golang/dep)
* [Golang](https://golang.org)
* [Docker Images](https://hub.docker.com/_/golang/)