import com.concur.*;

workflowDoc = '''
overview: Steps for building and testing with Golang.
additional_resources:
  - name: Rust
    url: https://www.rust-lang.org/en-US/
  - name: Cargo
    url: http://doc.crates.io
  - name: Docker Images
    url: https://hub.docker.com/_/rust/
tools:
  - type: String
    name: buildImage
    required: true
    description: Docker image containg tools for Rust.
  - type: List
    name: additionalArgs
    description: A list of additional flags to send to the cargo command.
  - type: List
    name: components
    description: Additional rustup components to install.
  - type: String
    name: command
    description: Which cargo command to execute.
    default: build
full_example: |
  pipelines:
    tools:
      branches:
        patterns:
          feature: .+
    branches:
      feature:
        steps:
          - rust:
            - cargo:
                command: build
'''

concurPipeline  = new Commands()
concurUtil      = new Util()

/*
description: Create a Pull Request in GitHub.
parameters:
  - type: String
    name: buildImage
    description: Docker image containg tools for Rust.
  - type: List
    name: additionalArgs
    description: A list of additional flags to send to the cargo command.
  - type: List
    name: components
    description: Additional rustup components to install.
  - type: String
    name: command
    description: Which cargo command to execute.
    default: build
example: |
  branches:
    feature:
      steps:
        - rust:
            # Simple
            - cargo:
            # Advanced
            - cargo:
                command: build
                title: Fix for issue {{ branch_name }}.
 */
public cargo(Map yml, Map args) {
  String buildImage     = args?.buildImage      ?: yml.tools?.rust?.buildImage
  List additionalArgs   = args?.additionalArgs  ?: yml.tools?.rust?.additionalArgs
  List rustupComponents = args?.components      ?: yml.tools?.rust?.components
  String command        = args?.command         ?: yml.tools?.rust?.command         ?: "build"

  assert buildImage : "Workflows :: rust :: cargo :: [buildImage] is needed in [tools.rust] or as a parameter to the test step."

  buildImage = concurUtil.mustacheReplaceAll(buildImage)

  String cargoCommand = "cargo ${command}"

  /**
   * Define additional args as any of the following
   * ----------------------------------------------
   * rust:
   *   - cargo:
   *       additionalArgs:
   *         - "--force"
   *         - "--skip-test"
   *         - "-v"
   * ----------------------------------------------
   */
  if (additionalArgs) {
    cargoCommand = "$cargoCommand ${additionalArgs.join(' ')}"
  }

  concurPipeline.debugPrint("Workflows :: rust :: cargo", [
    'buildImage'      : buildImage,
    'command'         : command,
    'additionalArgs'  : additionalArgs,
    'cargoCommand'    : cargoCommand
  ])

  // -u 0:0 runs as root, -v mounts the current workspace to your gopath
  docker.image(buildImage).inside("-v \"${pwd()}/.cargo:/usr/local/cargo/registry/:rw\"") {
    /*
      RustUp is a tool for managing Rust and its components
     */
    if (rustupComponents) {
      rustupComponents.each {
        sh "rustup component add $it"
      }
    }
    sh cargoCommand
  }
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'cargo':
      def cargoCommand = args?.command ?: "build"
      return "rust: cargo: ${cargoCommand}"
  }
}

public tests(Map yml, Map args) {
  String workflowName = 'rust'
  println "Testing $workflowName"
}

return this;
