#!/usr/bin/env groovy
import com.concur.*;

workflowDoc = '''
overview: Includes workflows for running various language independent build tools.
additional_resources:
  - name: Magefile GitHub
    url: https://github.com/magefile/mage
  - name: Magefile Official Docs
    url: https://magefile.org
tools:
  - type: String
    name: buildImage
    section: mage
    description: Docker image that has Mage installed
  - type: String
    name: target
    section: mage
    description: The mage target 
  - type: String
    name: mageFileDir
    section: mage
    default: '.'
    description: The directory containing your magefile.
full_example: |
  pipelines:
    tools:
      mage:
        buildImage: "quay.io/example/mage"
      branches:
        patterns:
          feature: .+
    branches:
      feature:
        steps:
          - build:
            - mage:
          - build:
            - mage:
                target: Docker
'''

concurUtil = new com.concur.Util()

/*
description: Execute mage targets.
parameters:
  - type: String
    name: buildImage
    description: Docker image that has Mage installed
  - type: String
    name: target
    description: The mage target 
  - type: String
    name: mageFileDir
    default: '.'
    description: The directory containing your magefile.
example:
  branches:
    feature:
      steps:
        - build:
            # Simple
            - mage:
            # Advanced
            - mage:
                target: Install
 */
public mage(Map yml, Map args) {
  String buildImage  = args?.buildImage  ?: yml.tools?.mage?.buildImage
  String target      = args?.target      ?: yml.tools?.mage?.target
  String mageFileDir = args?.mageFileDir ?: yml.tools?.mage?.mageFileDir ?: '.'

  def cmd = "mage"

  if (target) {
    cmd = "$cmd $target"
  }

  dir(mageFileDir) {
    docker.image(buildImage).inside {
      sh concurUtil.mustacheReplaceAll(cmd)
    }
  }
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'mage':
      def mageTarget = args?.target ?: yml.tools?.mage?.target
      return mageTarget ? "build: mage: ${mageTarget}" : "build: mage"
  }
}

return this;
