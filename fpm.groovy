import com.concur.*;

workflowDoc = '''
title: FPM
overview: Use the FPM tool to create packages for various systems.
additional_resources:
  - name: FPM
    url: https://github.com/jordansissel/fpm
tools:
  - type: String
    name: buildImage
    description: Docker image containing the FPM tools as well as any other requirements.
    required: true
  - type: String
    name: sourceType
    description: Refer to [Sources documentation](http://fpm.readthedocs.io/en/latest/sources.html).
    default: dir
  - type: String
    name: version
    description: Version number to use for the resulting package, eqivalent to the `-v` flag.
    default: "{{ build_version }}"
  - type: String
    name: name
    description: The name of the output package, format will be <name>-<version>.<target>.
    default: <repo>
  - type: String
    name: sourceDir
    description: When using the dir sourceType this is the directory that will get packaged.
  - type: String
    name: extraArgs
    description: Any extra arguments to the FPM command.
  - type: List
    name: dependencies
    description: A list of dependencies that are required by your output package.
  - type: List
    name: targetTypes
    description: Formats to create with the command.
    default: ['rpm']
full_example: |
  pipelines:
    tools:
      branches:
        patterns:
          master: master
          develop: develop
          feature: .+
    branches:
      feature:
        steps:
          - fpm:
            - package:
                targetTypes:
                  - deb
                  - rpm
          - artifactory:
            - publish:
'''

concurGit   = new Git()
concurUtil  = new Util()

/*
description: Create application packages for various systems with one tool.
parameters:
  - type: String
    name: buildImage
    description: Docker image containing the FPM tools as well as any other requirements.
    required: true
  - type: String
    name: sourceType
    description: Refer to [Sources documentation](http://fpm.readthedocs.io/en/latest/sources.html).
    default: dir
  - type: String
    name: version
    description: Version number to use for the resulting package, eqivalent to the `-v` flag.
    default: "{{ build_version }}"
  - type: String
    name: name
    description: The name of the output package, format will be <name>-<version>.<target>.
    default: <repo>
  - type: String
    name: sourceDir
    description: When using the dir sourceType this is the directory that will get packaged.
  - type: String
    name: extraArgs
    description: Any extra arguments to the FPM command.
  - type: List
    name: dependencies
    description: A list of dependencies that are required by your output package.
  - type: List
    name: targetTypes
    description: Formats to create with the command.
    default: ['rpm']
example: |
  branches:
    feature:
      steps:
        - ansible:
            # Simple
            - playbook:
            # Advanced
            - playbook:
                playbook: scripts/ansible/example-playbook.yml
                extraVars:
                  DOCKER_IMAGE: "{{ DOCKER_IMAGE_TAG }}"
                limit: qa
 */
public 'package'(Map yml, Map args) {
  def gitData         = concurGit.getGitData()
  String buildImage   = args?.buildImage    ?: yml.tools?.fpm?.buildImage
  String sourceType   = args?.sourceType    ?: yml.tools?.fpm?.sourceType   ?: 'dir'
  String version      = args?.version       ?: yml.tools?.fpm?.version      ?: "{{ build_version }}"
  String packageName  = args?.name          ?: yml.tools?.fpm?.name         ?: gitData.repo
  String sourceDir    = args?.sourceDir     ?: yml.tools?.fpm?.sourceDir
  String extraArgs    = args?.extraArgs     ?: yml.tools?.fpm?.extraArgs
  List dependencies   = args?.dependencies  ?: yml.tools?.fpm?.dependencies
  List targetTypes    = args?.targetTypes   ?: yml.tools?.fpm?.targetTypes  ?: ['rpm']

  assert sourceType   : "Workflows :: fpm :: package :: No [sourceType] provided in [tools.fpm] or as a parameter to the fpm.package step."
  assert targetTypes  : "Workflows :: fpm :: package :: No [targetTypes] provided in [tools.fpm] or as a parameter to the fpm.package step."
  assert sourceDir    : "Workflows :: fpm :: package :: No [sourceDir] provided in [tools.fpm] or as a parameter to the fpm.package step."
  assert buildImage   : "Workflows :: fpm :: package :: No [buildImage] provided in [tools.fpm] or as a parameter to the fpm.package step."

  buildImage = concurUtil.mustacheReplaceAll(buildImage)

  def fpmFlags = "-s ${sourceType}"

  if (sourceType == 'dir') {
    fpmFlags = "${fpmFlags} -C ${sourceDir}"
  }

  if (packageName) {
    fpmFlags = "${fpmFlags} -n ${packageName}"
  }

  if (version) {
    fpmFlags = "${fpmFlags} -v ${version}"
  }

  if (dependencies) {
    def joinedDependencies = dependencies.collect{ "-d ${it}" }.join(' ')
    fpmFlags = "${fpmFlags} ${joinedDependencies}"
  }

  if (extraArgs) {
    fpmFlags = "${fpmFlags} ${extraArgs}"
  }

  fpmFlags = concurUtil.mustacheReplaceAll(fpmFlags)

  docker.image(buildImage).inside {
    targetTypes.each {
      sh "fpm -t ${it} ${fpmFlags}"
    }
  }
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'package':
      def fpmCommand = args?.sourceType ?: yml.tools?.fpm?.sourceType
      return fpmCommand ? "fpm: package: ${fpmCommand}" : 'fpm: package'
  }
}

public tests(Map yml, Map args) {
  String workflowName = 'fpm'
  println "Testing $workflowName"
}

return this;
