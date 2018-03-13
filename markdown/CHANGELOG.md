# Jenkins Workflows

## v0.4.1

### Changed

* docker: Stage names changed to be more simple.

## v0.4.0

### Added

* Start of a test framework for workflows.

### Fixes

* golang.lint: --checkstyle being specified would cause failures
* golang.lint: Was not properly get values if specified under tools.golang

### Changed

* docker.push: Logging in changed to `echo $password | docker login $endpoint -u $user --password-stdin`
* docker.*: Stage names changed to reflect the image name instead of dockerfile.

## v0.3.0

### Added

* golang.lint: Lint Golang code during a pipeline execution.
* golang.dep: Support for the official dependency manager for Golang.

### Fixes

* docker.push: Wrong credential was being pulled.
* golang.getStageName: Test workflow failed due to type safety.
* nodejs & slack: ConcurCommands class name doesn't exist.

## v0.2.0

### Added

* Gradle workflow
* Slack workflow
* NodeJS workflow

### Updates

* git.commit: Checks for changes before attempting the commit, this didn't cause a failure before but is cleaner now.
* Add more asserts to workflows.

## v0.1.1

### Fixed

* github.createRelease: Fix issue where generated name/tag can get multiple `v`s for a prefix.

## 0.1.0

### Added

Please view the associated documentation on how to use these workflows, or visit [GitHub pages](https://concur.github.io/jenkins-workflows/) generated documentation.

* [Ansible](ANSIBLE.md)
* [Build](BUILD.md)
* [Docker](DOCKER.md)
* [Email](EMAIL.md)
* [Fpm](FPM.md)
* [Git](GIT.md)
* [Github](GITHUB.md)
* [Golang](GOLANG.md)
* [Python](PYTHON.md)
* [Rust](RUST.md)
