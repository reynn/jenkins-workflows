import com.concur.*;

workflowDoc = '''
overview: Run Python scripts.
additional_resources:
  - name: Official Docker images
    url: https://hub.docker.com/_/python/
tools:
  - type: String
    name: buildImage
    description: Docker image containing Python and any non requirements.txt dependencies.
    required: true
  - type: String
    name: binary
    description: The Python binary to use, for example `python2` or `python3`.
    required: false
    default: python
  - type: String
    name: file
    description: Path to a script to run, relative to the root of the project.
    required: true
  - type: String
    name: requirements
    description: Path to a requirements.txt file to install via Pip.
    default: requirements.txt
    required: false
  - type: List
    name: arguments
    description: List of arguments to the script, should include any flags if needed.
    required: false
full_example: |
  pipelines:
    tools:
      python:
        buildImage: "python:3.6-alpine3.7"
      branches:
        patterns:
          feature: .+
    branches:
      feature:
        steps:
          - python:
            - script:
                file: scripts/build.py
'''

concurPipeline = new Commands()
concurUtil = new Util()

/*
description: Run a Python script.
parameters:
  - type: String
    name: buildImage
    description: Docker image containing Python and any non requirements.txt dependencies.
    required: true
  - type: String
    name: binary
    description: The Python binary to use, for example `python2` or `python3`.
    required: false
    default: python
  - type: String
    name: file
    description: Path to a script to run, relative to the root of the project.
    required: true
  - type: String
    name: requirements
    description: Path to a requirements.txt file to install via Pip.
    default: requirements.txt
    required: false
  - type: List
    name: arguments
    description: List of arguments to the script, should include any flags if needed.
    required: false
example:
  branches:
    feature:
      steps:
        - python:
            # Simple
            - script:
            # Advanced
            - script:
                binary: python3
                script: scripts/update_docs.py
 */
public script(Map yml, Map args) {
  String dockerImage  = args?.buildImage    ?: yml.tools?.python?.buildImage
  String pythonBin    = args?.binary        ?: yml.tools?.python?.binary        ?: 'python'
  String file         = args?.file          ?: yml.tools?.python?.file
  String requirements = args?.requirements  ?: yml.tools?.python?.requirements
  List arguments      = args?.arguments     ?: yml.tools?.python?.arguments

  String pythonCommand = "$pythonBin $file"

  if (arguments) {
    pythonCommand = "$pythonCommand ${arguments.join(' ')}"
  }

  def requirementsFileExists = fileExists(requirements)

  dockerImage   = concurUtil.mustacheReplaceAll(dockerImage)
  pythonCommand = concurUtil.mustacheReplaceAll(pythonCommand)

  docker.image(dockerImage).inside {
    if (requirementsFileExists) {
      pythonCommand = "pip install -r $requirements && $pythonCommand"
    }
    sh "$pythonCommand"
  }
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'script':
      String file = args?.file ?: yml.tools?.python?.file
      return "python: script: $file"
  }
}

return this;
