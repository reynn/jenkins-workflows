# Docker

## Available Methods

### build

> Build a Docker image

| Name        | Type   | Default                                   | Description                                                                                                |
|:------------|:-------|:------------------------------------------|:-----------------------------------------------------------------------------------------------------------|
| dockerfile  | String |                                           | Path to a dockerfile to build, equivalent to `-f <dockerfile>`.                                            |
| imageName   | String | `<git_org>/<git_repo>`                    | What to name the image, equivalent to `-t <imageName>`.                                                    |
| imageTag    | String | `buildVersion`                            | What to name the image, equivalent to `-t <imageName>:<imageTag>`.                                         |
| contextPath | String | `.`                                       | Path to the directory to start the Docker build, equivalent to the final argument to docker build command. |
| vcsUrl      | String | `https://<git_host>/<git_org>/<git_repo>` |                                                                                                            |
| buildArgs   | Map    |                                           | A map of arguments to pass to docker build command, equivalent to `--build-arg <key>=<value>`              |

### build Example

```yaml
branches:
  feature:
    steps:
    - docker:
      - build: null
      - build:
          buildArgs:
            BuildDate: '{{ timestamp }}'
            BuildVersion: '{{ build_version }}'
            CommitSha: '{{ git_commit }}'
          dockerfile: production.dockerfile
```

### push

> Push a Docker image to a remote registry.

| Name           | Type   | Default                | Description                                                            |
|:---------------|:-------|:-----------------------|:-----------------------------------------------------------------------|
| imageName      | String | `<git_org>/<git_repo>` | The name of the image to push.                                         |
| imageTag       | String | `buildVersion`         | Tag of the image to push.                                              |
| uri            | String |                        | The uri of the registry to push to, such as quay.io or hub.docker.com. |
| additionalTags | List   |                        | A list of tags to push in addition to `imageTag` above.                |
| credentials    | Map    |                        | A map of criteria to use to search for your credential                 |

### push Example

```yaml
branches:
  feature:
    steps:
    - docker:
      - push: null
      - push:
          additionalTags:
          - '{{ git_commit }}'
          credentials:
            description: example docker creds
```

## Full Example Pipeline

```yaml
pipelines:
  branches:
    feature:
      steps:
      - docker:
        - build:
            buildArgs:
              BuildDate: '{{ timestamp }}'
              BuildVersion: '{{ build_version }}'
              CommitSha: '{{ git_commit }}'
        - push:
            additionalTags:
            - '{{ git_commit }}'
  tools:
    branches:
      patterns:
        feature: .+
    docker:
      credentials:
        description: example docker creds
      dockerfile: production.dockerfile
```

## Additional Resources

* [Promotion - Artifactory](https://wiki.concur.com/confluence/display/DA/Promotion+-+Artifactory)
* [Docker build documentation](https://docs.docker.com/engine/reference/commandline/build/)
* [Docker push documentation](https://docs.docker.com/engine/reference/commandline/push/)