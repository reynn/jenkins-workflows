import org.codehaus.groovy.runtime.GStringImpl;

concurPipeline  = new com.concur.Commands()
concurUtil      = new com.concur.Util()
concurGit       = new com.concur.Git()

public build(Map yml, Map args) {
  String baseVersion  = yml.general?.version?.base  ?: "0.1.0"
  String buildVersion = concurGit.getVersion(baseVersion)

  String dockerfile = args?.dockerfile            ?: yml.tools?.docker?.dockerfile
  String imageName  = args?.imageName             ?: yml.tools?.docker?.imageName   ?: "${env.GIT_ORG}/${env.GIT_REPO}"
  String imageTag   = args?.imageTag              ?: yml.tools?.docker?.imageTag    ?: buildVersion
  String context    = args?.contextPath           ?: yml.tools?.docker?.contextPath ?: '.'
  String vcsUrl     = args?.vcsUrl                ?: yml.tools?.github?.uri         ?: "https://${GIT_HOST}/${env.GIT_ORG}/${env.GIT_REPO}"
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
    'baseVersion'   : baseVersion,
    'buildVersion'  : buildVersion,
    'imageTag'      : imageTag,
    'additionalArgs': additionalArgs,
    'fullImageName' : fullImageName,
    'vcsUrl'        : vcsUrl
  ])

  docker.build(fullImageName, additionalArgs)
}

public push(Map yml, Map args) {
  String baseVersion    = yml.general?.version?.base ?: "0.1.0"
  String buildVersion   = concurGit.getVersion(baseVersion)
  String imageName      = args?.imageName      ?: yml.tools?.docker?.imageName      ?: "${env.GIT_ORG}/${env.GIT_REPO}"
  String imageTag       = args?.imageTag       ?: yml.tools?.docker?.imageTag       ?: buildVersion
  String dockerEndpoint = args?.uri            ?: yml.tools?.docker?.uri            ?: env.DOCKER_URI
  List additionalTags   = args?.additionalTags ?: yml.tools?.docker?.additionalTags ?: []
  Map credentials       = args?.credentials    ?: yml.tools?.docker?.credentials    ?: [:]

  def dockerCredentialId

  assert imageName  : "No [imageName] provided in [tools.docker] or as a parameter to the docker.push step."
  assert imageTag   : "No [imageTag] provided in [tools.docker] or as a parameter to the docker.push step."

  dockerEndpoint = concurUtil.mustacheReplaceAll(dockerEndpoint)

  if (credentials != null) {
    assert (credentials instanceof Map) :
    """|Credentials are provided either in [tools.docker.credentials] or as a parameter to this step.
       |The data provided is not a map.
       |Credentials should be defined in your pipelines.yml as:
       |----------------------------------------
       |tools:
       |  docker:
       |    credentials:
       |      description: example docker credentials""".stripMargin()
    dockerCredentialId = concurPipeline.getCredentialsWithCriteria(credentials).id
  }

  def fullImageName = concurUtil.mustacheReplaceAll("${dockerEndpoint}/${imageName}:${imageTag}")

  concurPipeline.debugPrint("Workflows :: Docker :: Push", [
    'baseVersion'         : baseVersion,
    'imageName'           : imageName,
    'buildVersion'        : buildVersion,
    'imageTag'            : imageTag,
    'fullImageName'       : fullImageName,
    'dockerEndpoint'      : dockerEndpoint,
    'dockerCredentialId'  : dockerCredentialId,
    'additionalTags'      : additionalTags
  ])

  println "image not pushed"
  withCredentials([usernamePassword(credentialsId: dockerCredentialId, passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
    // using this instead of withRegistry because of various errors encountered when using it in production.
    sh "docker logout ${dockerEndpoint}"
    sh "docker tag ${imageName}:${imageTag} ${fullImageName}"
    sh "docker login ${dockerEndpoint} -u ${env.DOCKER_USERNAME} -p ${env.DOCKER_PASSWORD}"
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
 ******************************* COMMON *******************************
 This a section for common utilities being called from the runSteps method in com.concur.Commands
 */

public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'build':
      def dockerfile = args?.dockerfile ?: yml.tools?.docker?.dockerfile
      return dockerfile ? "docker: build: ${dockerfile}": 'docker: build'
    case 'push':
      def dockerUri = args?.uri ?: yml.tools?.docker?.uri
      return dockerUri ? "docker: push: ${dockerUri}" : 'docker: push'
  }
}

return this;
