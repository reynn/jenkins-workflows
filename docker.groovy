import org.codehaus.groovy.runtime.GStringImpl;

workflowDoc = '''
additional_resources:
  - name: Promotion - Artifactory
    url: https://wiki.concur.com/confluence/display/DA/Promotion+-+Artifactory
  - name: Docker build documentation
    url: https://docs.docker.com/engine/reference/commandline/build/
  - name: Docker push documentation
    url: https://docs.docker.com/engine/reference/commandline/push/
tools:
  - type: String
    name: dockerfile
    section: docker
    description: Path to a dockerfile to build, equivalent to `-f <dockerfile>`.
  - type: String
    name: imageName
    section: docker
    default: "<git_owner>/<git_repo>"
    description: What to name the image, equivalent to `-t <imageName>`.
  - type: String
    name: imageTag
    section: docker
    default: buildVersion
    description: What to name the image, equivalent to `-t <imageName>:<imageTag>`.
  - type: String
    name: contextPath
    section: docker
    default: .
    description: Path to the directory to start the Docker build, equivalent to the final argument to docker build command.
  - type: String
    name: uri
    section: github
    default: https://<git_host>/<git_owner>/<git_repo>
  - type: Map
    name: buildArgs
    section: docker
    description: A map of arguments to pass to docker build command, equivalent to `--build-arg <key>=<value>`.
  - type: String
    name: uri
    section: docker
    description: The uri of the registry to push to, such as quay.io, if not provided it will generally push to Docker hub.
  - type: List
    name: additionalTags
    section: docker
    description: A list of tags to push in addition to `imageTag` above.
  - type: Map
    name: credentials
    section: docker
    description: A map of criteria to use to search for your credential.
full_example: |
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
'''

concurPipeline  = new com.concur.Commands()
concurUtil      = new com.concur.Util()
concurGit       = new com.concur.Git()

/*
description: Build a Docker image.
parameters:
  - type: String
    name: dockerfile
    description: Path to a dockerfile to build, equivalent to `-f <dockerfile>`.
  - type: String
    name: imageName
    default: "<git_owner>/<git_repo>"
    description: What to name the image, equivalent to `-t <imageName>`.
  - type: String
    name: imageTag
    default: buildVersion
    description: What to name the image, equivalent to `-t <imageName>:<imageTag>`.
  - type: String
    name: contextPath
    default: .
    description: Path to the directory to start the Docker build, equivalent to the final argument to docker build command.
  - type: String
    name: vcsUrl
    default: https://<git_host>/<git_owner>/<git_repo>
  - type: Map
    name: buildArgs
    description: A map of arguments to pass to docker build command, equivalent to `--build-arg <key>=<value>`.
example:
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
 */
public build(Map yml, Map args) {
  String buildVersion = concurGit.getVersion(yml)

  String dockerfile = args?.dockerfile            ?: yml.tools?.docker?.dockerfile
  String imageName  = args?.imageName             ?: yml.tools?.docker?.imageName   ?: "${env.GIT_OWNER}/${env.GIT_REPO}"
  String imageTag   = args?.imageTag              ?: yml.tools?.docker?.imageTag    ?: buildVersion
  String context    = args?.contextPath           ?: yml.tools?.docker?.contextPath ?: "."
  String vcsUrl     = args?.vcsUrl                ?: yml.tools?.github?.uri         ?: "https://${GIT_HOST}/${env.GIT_OWNER}/${env.GIT_REPO}"
  Map buildArgs     = args?.buildArgs             ?: yml.tools?.docker?.buildArgs   ?: [:]

  String additionalArgs = ""

  if (dockerfile) {
    additionalArgs = "${additionalArgs} --file ${dockerfile}"
  }

  if (buildArgs) {
    additionalArgs = "${additionalArgs} ${buildArgs.collect { "--build-arg ${it.key}=${it.value}" }.join(' ')}"
  }

  additionalArgs = concurUtil.mustacheReplaceAll("${additionalArgs} ${context}", [
    'VCS_URL'       : vcsUrl
  ])

  String fullImageName = concurUtil.mustacheReplaceAll("${imageName}:${imageTag}")

  concurPipeline.debugPrint('Workflows :: docker :: build', [
    'dockerfile'    : dockerfile,
    'buildArgs'     : buildArgs,
    'imageName'     : imageName,
    'buildVersion'  : buildVersion,
    'imageTag'      : imageTag,
    'additionalArgs': additionalArgs,
    'fullImageName' : fullImageName,
    'vcsUrl'        : vcsUrl
  ])

  docker.build(fullImageName, additionalArgs)
}

/*
description: Push a Docker image to a remote registry.
parameters:
  - name: imageName
    type: String
    default: "<git_owner>/<git_repo>"
    description: The name of the image to push.
  - name: imageTag
    type: String
    default: buildVersion
    description: Tag of the image to push.
  - name: uri
    type: String
    description: The uri of the registry to push to, such as quay.io or hub.docker.com.
  - name: additionalTags
    type: List
    description: A list of tags to push in addition to `imageTag` above.
  - name: credentials
    type: Map
    description: A map of criteria to use to search for your credential.
example:
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
 */
public push(Map yml, Map args) {
  String buildVersion   = concurGit.getVersion(yml)
  String imageName      = args?.imageName      ?: yml.tools?.docker?.imageName      ?: "${env.GIT_OWNER}/${env.GIT_REPO}"
  String imageTag       = args?.imageTag       ?: yml.tools?.docker?.imageTag       ?: buildVersion
  String dockerEndpoint = args?.uri            ?: yml.tools?.docker?.uri            ?: env.DOCKER_URI
  List additionalTags   = args?.additionalTags ?: yml.tools?.docker?.additionalTags ?: []
  Map credentials       = args?.credentials    ?: yml.tools?.docker?.credentials    ?: [:]

  assert imageName    : 'Workflows :: docker :: push :: No [imageName] provided in [tools.docker] or as a parameter to the docker.push step.'
  assert imageTag     : 'Workflows :: docker :: push :: No [imageTag] provided in [tools.docker] or as a parameter to the docker.push step.'
  assert credentials  : 'Workflows :: docker :: push :: No [credentials] provided in [tools.docker] or as a parameter to the docker.push step.'

  dockerEndpoint = concurUtil.mustacheReplaceAll(dockerEndpoint)

  def dockerCredentialId = concurPipeline.getCredentialsWithCriteria(credentials).id

  def fullImageName = concurUtil.mustacheReplaceAll("${dockerEndpoint}/${imageName}:${imageTag}")

  concurPipeline.debugPrint("Workflows :: docker :: push", [
    'imageName'           : imageName,
    'buildVersion'        : buildVersion,
    'imageTag'            : imageTag,
    'fullImageName'       : fullImageName,
    'dockerEndpoint'      : dockerEndpoint,
    'credentials'         : credentials,
    'dockerCredentialId'  : dockerCredentialId,
    'additionalTags'      : additionalTags
  ])

  withCredentials([usernamePassword(credentialsId: dockerCredentialId, passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
    // using this instead of withRegistry because of various errors encountered when using it in production.
    sh "docker logout ${dockerEndpoint}"
    sh "docker tag ${imageName}:${imageTag} ${fullImageName}"
    // this will avoid warning when logging in [WARNING! Using --password via the CLI is insecure. Use --password-stdin.]
    sh "echo -n '${env.DOCKER_PASSWORD}' | docker login ${dockerEndpoint} -u '${env.DOCKER_USERNAME}' --password-stdin"
    docker.image(fullImageName).push()
    if (additionalTags) {
      assert (additionalTags instanceof List) : "Workflows :: Docker :: Push :: additionalTags provided but not as a list."
      additionalTags.each {
        docker.image(fullImageName).push(concurUtil.kebab(concurUtil.mustacheReplaceAll(it)))
      }
    }
  }
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  String buildVersion = concurGit.getVersion(yml)
  switch(stepName) {
    case 'build':
      def dockerfile = args?.dockerfile ?: yml.tools?.docker?.dockerfile
      return dockerfile ? "docker: build: $dockerfile": 'docker: build'
    case 'push':
      String imageTag     = args?.imageTag  ?: yml.tools?.docker?.imageTag  ?: buildVersion
      String imageName    = args?.imageName ?: yml.tools?.docker?.imageName ?: "${env.GIT_OWNER}/${env.GIT_REPO}"
      return "docker: push: $imageName:$imageTag"
  }
}

return this;
