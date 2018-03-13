# jenkins-workflows

## Purpose

To be used in conjunction with the [jenkins-yml-workflow](https://github.com/concur/jenkins-yml-workflowLibs) repository in a Jenkins repository. These are steps to take for interacting with a specific tool.

## Creating Workflows

Basic structure of how the file should be laid out can be found in the [example.groovy](../example.groovy) file. There are some important things to remember here:

1. The `return this;` is required for Groovy to load anything.
1. Methods must be using `public`, `def` is not sufficient due to the way the Groovy load method works.
1. The [workflowLibs](https://github.com/concur/jenkins-yml-workflowLibs) when added as a global library is running outside of the Jenkins sandbox. Workflows will be inside the sandbox though so non white-listed methods cannot be used without approval.
1. Private methods are perfectly acceptable as long as they are to be called within a public method.

## Where to add new workflows

If the tool can be used for multiple languages (Make/Mage) it should be grouped with similar tools if possible, for instance [Glide](https://github.com/Masterminds/glide) and [Godep](https://github.com/tools/godep) are tools for Golang dependency management so they would go in a `golang.groovy` file. Make and Mage (a makefile alternative that allows you to use Go instead of Bash) would go into a `build.groovy`. If you are unsure of where to put a new workflow feel free to open an issue and we can help determine the best place for it.

## Locking to a release

We highly suggest locking to a release so you always get consistent build results. We will never modify tags or commit SHAs after a release happens. This also helps allow the workflows to evolve rapidly without breaking you. Patch versions will never have breaking changes but major/minor could though we will make efforts to mitigate this as much as possible.

Example of how to lock to a release:

```yaml
pipelines:
  tools:
    jenkins:
      workflows:
        tag: 0.1.0 # Example, check the latest release from https://github.com/concur/jenkins-workflows/releases
```

## Contributing

We welcome your contributions and request that you please make sure that you look at the [contributing guidelines](contributing) before contributing. If you have a question on a feature proposal, or a bug fix that you'd like to address, please create an issue so that contributors can discuss and collaborate.
