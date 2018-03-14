import com.concur.*;

concurPipeline  = new Commands()
concurUtil      = new Util()

workflowDoc = '''
overview: Execute any NPM, Grunt or Gulp task.
additional_resources:
  - name: NodeJS Official Site
    url: https://nodejs.org/en/
  - name: NPM
    url: https://www.npmjs.com
  - name: Grunt
    url: https://gruntjs.com
  - name: Gulp
    url: https://gulpjs.com
  - name: Docker Images
    url: https://hub.docker.com/_/node/
tools:
  - type: String
    name: dockerImage
    section: nodejs
    description: Docker image to run all NodeJS commands in.
  - type: List
    name: commandArgs
    section: nodejs.npm
    description: Additional arguments to the NPM commands.
  - type: String
    name: command
    section: nodejs.npm
    description: The NPM command to run within a nodejs.npm workflow step.
    default: install
  - type: String
    name: npmRegistry
    section: nodejs.npm
    description: URL to an alternate NPM registry.
  - type: List
    name: commandArgs
    section: nodejs.gulp
    description: Additional arguments to a Gulp command.
  - type: String
    name: command
    section: nodejs.gulp
    description: The Gulp command to run within a nodejs.gulp workflow step.
    default: install
  - type: List
    name: commandArgs
    section: nodejs.grunt
    description: Additional arguments to a Grunt command.
  - type: String
    name: command
    section: nodejs.grunt
    description: The Grunt command to run within a nodejs.grunt workflow step.
    default: install
full_example: |
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
'''

/*
description: Execute NPM tasks.
parameters:
  - type: String
    name: dockerImage
    description: Docker image to run all NodeJS commands in.
  - type: List
    name: commandArgs
    description: Additional arguments to the NPM commands.
  - type: String
    name: command
    description: The NPM command to run within a nodejs.npm workflow step.
    default: install
  - type: String
    name: npmRegistry
    description: URL to an alternate NPM registry.
example: |
  branches:
    feature:
      steps:
        - nodejs:
            # Simple
            - node:
            # Advanced
            - node:
                command: compile
 */
public npm(Map yml, Map args) {
  String dockerImage  = args?.buildImage  ?: yml.tools?.nodejs?.buildImage
  List commandArgs    = args?.extraArgs   ?: yml.tools?.nodejs?.npm?.extraArgs
  String command      = args?.command     ?: yml.tools?.nodejs?.npm?.command     ?: 'install'
  // default to the npm cache from Artifactory
  String npmRegistry  = args?.registry    ?: yml.tools?.nodejs?.npm?.registry

  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)
  npmRegistry = concurUtil.mustacheReplaceAll(npmRegistry)

  assert dockerImage : 'Workflows :: nodejs :: npm :: No [buildImage] provided in [tools.nodejs] or as a parameter to the nodejs.npm step.'

  concurPipeline.debugPrint('Workflows :: nodejs :: npm', [
    'args'        : args,
    'dockerImage' : dockerImage,
    'commandArgs' : commandArgs,
    'command'     : command,
    'npmRegistry' : npmRegistry
  ])

  docker.image(dockerImage).inside('-u 0:0') {
    if (npmRegistry) {
      sh "npm config set registry $npmRegistry -g"
    }
    command = "npm $command"
    if (commandArgs) {
      command = "$command ${commandArgs.join(' ')}"
    }
    sh command
  }
}

/*
description: Execute Gulp tasks.
parameters:
  - type: String
    name: dockerImage
    description: Docker image to run all NodeJS commands in.
  - type: List
    name: commandArgs
    description: Additional arguments to a Gulp command.
  - type: String
    name: command
    description: The Gulp command to run within a nodejs.gulp workflow step.
    default: install
example: |
  branches:
    feature:
      steps:
        - nodejs:
            # Simple
            - gulp:
            # Advanced
            - gulp:
                name: compileScss
 */
public gulp(Map yml, Map args) {
  String dockerImage  = args?.buildImage  ?: yml.tools?.nodejs?.buildImage
  List commandArgs    = args?.extraArgs   ?: yml.tools?.nodejs?.gulp?.extraArgs
  String command      = args?.command     ?: yml.tools?.nodejs?.gulp?.command   ?: 'install'

  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)

  assert dockerImage  : "Workflows :: nodejs :: gulp :: No [buildImage] provided in [tools.nodejs] or as a parameter to the nodejs.gulp step."

  concurPipeline.debugPrint('Workflows :: nodejs :: gulp', [
    'args'        : args,
    'dockerImage' : dockerImage,
    'commandArgs' : commandArgs,
    'command'     : command
  ])

  docker.image(dockerImage).inside('-u 0:0') {
    command = "gulp $command"
    if (commandArgs) {
      command = "$command ${commandArgs.join(' ')}"
    }
    sh command
  }
}

/*
description: Execute Grunt tasks.
parameters:
  - type: String
    name: dockerImage
    description: Docker image to run all NodeJS commands in.
  - type: List
    name: commandArgs
    description: Additional arguments to a Grunt command.
  - type: String
    name: command
    description: The Grunt command to run within a nodejs.grunt workflow step.
    default: install
example: |
  branches:
    feature:
      steps:
        - nodejs:
            # Simple
            - grunt:
            # Advanced
            - grunt:
                name: webpack
 */
public grunt(Map yml, Map args) {
  String dockerImage  = args?.buildImage  ?: yml.tools?.nodejs?.buildImage
  List commandArgs    = args?.extraArgs   ?: yml.tools?.nodejs?.grunt?.extraArgs
  String command      = args?.command     ?: yml.tools?.nodejs?.grunt?.command   ?: 'install'

  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)

  assert dockerImage  : "Workflows :: nodejs :: grunt :: No [buildImage] provided in [tools.nodejs] or as a parameter to the nodejs.grunt step."

  concurPipeline.debugPrint('Workflows :: nodejs :: grunt', [
    'args'        : args,
    'dockerImage' : dockerImage,
    'commandArgs' : commandArgs,
    'command'     : command
  ])

  docker.image(dockerImage).inside('-u 0:0') {
    command = "grunt $command"
    if (commandArgs) {
      command = "$command ${commandArgs.join(' ')}"
    }
    sh command
  }
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'npm':
      String command = args?.command ?: yml.tools?.nodejs?.npm?.command ?: 'install'
      return "nodejs: npm: ${command}"
    case 'gulp':
      String command = args?.command ?: yml.tools?.nodejs?.gulp?.command ?: 'install'
      return "nodejs: gulp: ${command}"
    case 'grunt':
      String command = args?.command ?: yml.tools?.nodejs?.grunt?.command ?: 'install'
      return "nodejs: grunt: ${command}"
  }
}

public tests(Map yml, Map args) {
  String workflowName = 'nodejs'
  println "Testing $workflowName"
}

return this;
