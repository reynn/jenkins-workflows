# Docker

## Overview

> Build, tag and push Docker images to any registry.

## Tools Section

| Name           | Type   | Default                                     | Section   | Description                                                                                                |
|:---------------|:-------|:--------------------------------------------|:----------|:-----------------------------------------------------------------------------------------------------------|
| dockerfile     | String |                                             | docker    | Path to a dockerfile to build, equivalent to `-f <dockerfile>`.                                            |
| imageName      | String | `<git_owner>/<git_repo>`                    | docker    | What to name the image, equivalent to `-t <imageName>`.                                                    |
| imageTag       | String | `buildVersion`                              | docker    | What to name the image, equivalent to `-t <imageName>:<imageTag>`.                                         |
| contextPath    | String | `.`                                         | docker    | Path to the directory to start the Docker build, equivalent to the final argument to docker build command. |
| uri            | String | `https://<git_host>/<git_owner>/<git_repo>` | github    |                                                                                                            |
| buildArgs      | Map    |                                             | docker    | A map of arguments to pass to docker build command, equivalent to `--build-arg <key>=<value>`.             |
| uri            | String |                                             | docker    | The uri of the registry to push to, such as quay.io, if not provided it will generally push to Docker hub. |
| additionalTags | List   |                                             | docker    | A list of tags to push in addition to `imageTag` above.                                                    |
| credentials    | Map    |                                             | docker    | A map of criteria to use to search for your credential.                                                    |

## Available Methods

### build

> Build a Docker image.

| Name        | Type   | Default                                     | Description                                                                                                |
|:------------|:-------|:--------------------------------------------|:-----------------------------------------------------------------------------------------------------------|
| dockerfile  | String |                                             | Path to a dockerfile to build, equivalent to `-f <dockerfile>`.                                            |
| imageName   | String | `<git_owner>/<git_repo>`                    | What to name the image, equivalent to `-t <imageName>`.                                                    |
| imageTag    | String | `buildVersion`                              | What to name the image, equivalent to `-t <imageName>:<imageTag>`.                                         |
| contextPath | String | `.`                                         | Path to the directory to start the Docker build, equivalent to the final argument to docker build command. |
| vcsUrl      | String | `https://<git_host>/<git_owner>/<git_repo>` |                                                                                                            |
| buildArgs   | Map    |                                             | A map of arguments to pass to docker build command, equivalent to `--build-arg <key>=<value>`.             |

### build Example

```yaml
branches:
  feature:
    steps:
      - docker:
          # Simple
          - build:
          # Advanced
          - build:
              dockerfile: production.dockerfile
              buildArgs:
                CommitSha: "{{ git_commit }}"
                BuildDate: "{{ timestamp }}"
                BuildVersion: "{{ build_version }}"
```

### push

> Push a Docker image to a remote registry.

| Name           | Type   | Default                  | Description                                                            |
|:---------------|:-------|:-------------------------|:-----------------------------------------------------------------------|
| imageName      | String | `<git_owner>/<git_repo>` | The name of the image to push.                                         |
| imageTag       | String | `buildVersion`           | Tag of the image to push.                                              |
| uri            | String |                          | The uri of the registry to push to, such as quay.io or hub.docker.com. |
| additionalTags | List   |                          | A list of tags to push in addition to `imageTag` above.                |
| credentials    | Map    |                          | A map of criteria to use to search for your credential.                |

### push Example

```yaml
branches:
  feature:
    steps:
      - docker:
          # Simple
          - push:
          # Advanced
          - push:
              credentials:
                description: example docker creds.
              additionalTags:
                - "{{ git_commit }}"
```

## Full Example Pipeline

```yaml
pipelines:
  tools:
    docker:
      credentials:
        description: example docker creds.
    branches:
      patterns:
        feature: .+
  branches:
    feature:
      steps:
        - docker: # This should be your build process
          - build:
              dockerfile: production.dockerfile
              buildArgs:
                CommitSha: "{{ git_commit }}"
                BuildDate: "{{ timestamp }}"
                BuildVersion: "{{ build_version }}"
          - push:
              additionalTags:
                - "{{ git_commit }}"
```

## Additional Resources

* [Promotion - Artifactory](https://wiki.concur.com/confluence/display/DA/Promotion+-+Artifactory)
* [Docker build documentation](https://docs.docker.com/engine/reference/commandline/build/)
* [Docker push documentation](https://docs.docker.com/engine/reference/commandline/push/)