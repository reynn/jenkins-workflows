# Rust

## Overview

> Steps for building and testing with Golang.

## Tools Section

| Name           | Type   | Default   | Description                                              |
|:---------------|:-------|:----------|:---------------------------------------------------------|
| buildImage     | String |           | Docker image containg tools for Rust.                    |
| additionalArgs | List   |           | A list of additional flags to send to the cargo command. |
| components     | List   |           | Additional rustup components to install.                 |
| command        | String | `build`   | Which cargo command to execute.                          |

## Available Methods

### cargo

> Create a Pull Request in GitHub.

| Name           | Type   | Default   | Description                                              |
|:---------------|:-------|:----------|:---------------------------------------------------------|
| buildImage     | String |           | Docker image containg tools for Rust.                    |
| additionalArgs | List   |           | A list of additional flags to send to the cargo command. |
| components     | List   |           | Additional rustup components to install.                 |
| command        | String | `build`   | Which cargo command to execute.                          |

### cargo Example

```yaml
branches:
  feature:
    steps:
    - rust:
      - cargo: null
      - cargo:
          command: build
          title: Fix for issue {{ branch_name }}.
```

## Full Example Pipeline

```yaml
pipelines:
  branches:
    feature:
      steps:
      - rust:
        - cargo:
            command: build
  tools:
    branches:
      patterns:
        feature: .+
```

## Additional Resources

* [Rust](https://www.rust-lang.org/en-US/)
* [Cargo](http://doc.crates.io)