import com.concur.*;

workflowDoc = '''
overview: Execute Ansible playbooks within a pipeline.
additional_resources:
  - name: Ansible
    url: https://docs.ansible.com/ansible/latest/playbooks.html
  - name: Docker Images
    url: https://hub.docker.com/ansible/ansible/
tools:
  - type: String
    name: buildImage
    required: true
    description: Docker image that has Ansible installed.
  - type: String
    name: playbook
    required: true
    description: Path to the playbook file used during this run.
  - type: String
    name: inventory
    required: true
    description: Path to an inventory file to run the playbook against.
  - type: String
    name: limit
    required: false
    description: Equivalent to `-l` or `--limit` only run against specific host groups.
  - type: String
    name: sudoUser
    required: false
    default: root
    description: Equivalent to `--become-user`.
  - type: Map
    name: credentials
    required: false
    description: Key/Value pair of the credentials to use when running the playbook.
  - type: Map
    name: extraVars
    required: false
    description: equivalent to `-e` or `--extra-vars` overwrite variables.
  - type: List
    name: tags
    required: false
    description: Run only specific tags during the playbook run.
  - type: List
    name: skippedTags
    required: false
    description: Skip the specified tags during the playbook run.
  - type: List
    name: extras
    required: false
    description: Additional arguments to the `ansible-playbook` command.
  - type: Boolean
    name: sudo
    required: false
    default: false
    description: Equivalent to `-b` or `--become`.
  - type: int
    name: forks
    required: false
    default: 10
    description: Equivalent to `-f` or `--forks` specify number of parallel processes to use.
  - type: int
    name: verbosity
    required: false
    description: Levels of verbose output to have. Example setting this to 2 would be the equivalent of -vv.
full_example: |
  pipelines:
    tools:
      ansible:
        credentials:
          description: "SSH deploy credentials"
        buildImage: "{{ quay_uri }}/da-workflow/ansible-alpine:2.4.1.0"
        playbook: "ansible/playbooks/app_deploy.yml"
        inventory: "ansible/app_inventory.yml"
      branches:
        patterns:
          master: master
          develop: develop
          feature: .+
    branches:
      feature:
        steps:
          - custom: # This should be your build process
            - buildPackage:
          - ansible:
            - playbook:
                limit: staging
      master:
        steps:
          - github:
            - createRelease:
          - ansible:
            - playbook:
                limit: production
'''

concurPipeline  = new Commands()
concurUtil      = new Util()
concurGit       = new Git()

/*
description: Execute an Ansible playbook.
parameters:
  - type: String
    name: buildImage
    required: true
    description: Docker image that has Ansible installed.
  - type: String
    name: playbook
    required: true
    description: Path to the playbook file used during this run.
  - type: String
    name: inventory
    required: true
    description: Path to an inventory file to run the playbook against.
  - type: String
    name: limit
    required: false
    description: Equivalent to `-l` or `--limit` only run against specific host groups.
  - type: String
    name: sudoUser
    required: false
    default: root
    description: Equivalent to `--become-user`.
  - type: Map
    name: credentials
    required: false
    description: Key/Value pair of the credentials to use when running the playbook.
  - type: Map
    name: extraVars
    required: false
    description: equivalent to `-e` or `--extra-vars` overwrite variables.
  - type: List
    name: tags
    required: false
    description: Run only specific tags during the playbook run.
  - type: List
    name: skippedTags
    required: false
    description: Skip the specified tags during the playbook run.
  - type: List
    name: extras
    required: false
    description: Additional arguments to the `ansible-playbook` command.
  - type: Boolean
    name: sudo
    required: false
    default: false
    description: Equivalent to `-b` or `--become`.
  - type: int
    name: forks
    required: false
    default: 10
    description: Equivalent to `-f` or `--forks` specify number of parallel processes to use.
  - type: int
    name: verbosity
    required: false
    description: Levels of verbose output to have. Example setting this to 2 would be the equivalent of -vv.
example:
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
public playbook(Map yml, Map args) {
  String dockerImage  = args?.buildImage  ?: yml.tools?.ansible?.buildImage
  String playbook     = args?.playbook    ?: yml.tools?.ansible?.playbook
  String inventory    = args?.inventory   ?: yml.tools?.ansible?.inventory
  String limit        = args?.limit       ?: yml.tools?.ansible?.limit
  String sudoUser     = args?.sudoUser    ?: yml.tools?.ansible?.sudoUser     ?: 'root'
  Map credentialDef   = args?.credentials ?: yml.tools?.ansible?.credentials  ?: yml.defaults?.credentials
  Map extraVars       = args?.extraVars   ?: yml.tools?.ansible?.extraVars    ?: ""
  List tags           = args?.tags        ?: yml.tools?.ansible?.tags         ?: ""
  List skippedTags    = args?.skippedTags ?: yml.tools?.ansible?.skippedTags  ?: ""
  List extras         = args?.extras      ?: yml.tools?.ansible?.extras       ?: ""
  Boolean sudo        = args?.sudo        ?: yml.tools?.ansible?.sudo         ?: false
  int forks           = args?.forks       ?: yml.tools?.ansible?.forks        ?: 10
  int ymlVerbosity    = args?.verbosity   ?: yml.tools?.ansible?.verbosity    ?: ""

  def verbosity = 2
  if (ymlVerbosity) {
    verbosity = ymlVerbosity
  }

  concurPipeline.debugPrint('Workflows :: ansible :: playbook', [
    'dockerImage'   : dockerImage,
    'playbook'      : playbook,
    'inventory'     : inventory,
    'extras'        : extras,
    'verbosity'     : verbosity,
    'extraVars'     : extraVars,
    'tags'          : tags,
    'skippedTags'   : skippedTags,
    'limit'         : limit,
    'sudo'          : sudo,
    'sudoUser'      : sudoUser,
    'forks'         : forks
  ])

  assert dockerImage      : "Workflows :: ansible :: playbook :: [buildImage] not provided in [tools.ansible] or as a parameter to the ansible.playbook step."
  assert playbook         : "Workflows :: ansible :: playbook :: [playbook] not provided in [tools.ansible] or as a parameter to the ansible.playbook step."
  assert inventory        : "Workflows :: ansible :: playbook :: [inventory] not provided in [tools.ansible] or as a parameter to the ansible.playbook step."
  assert credentialDef    : "Workflows :: ansible :: playbook :: [credentials] not provided in [tools.ansible] or as a parameter to the ansible.playbook step."

  def credential = concurPipeline.getCredentialsWithCriteria(credentialDef)

  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)

  if (extraVars) {
    def vars = extraVars.collect { "-e ${it.key}=\"${it.value}\"" }
    extras = "$extras ${vars.join(' ')}"
  }
  if (verbosity > 0) {
    extras = "$extras -${'v'*verbosity}"
  }

  if (extras) {
    extras = concurUtil.mustacheReplaceAll(extras)
  }

  docker.image(dockerImage).inside('-u 0:0') {
    if (!concurUtil.binAvailable('ansible')) {
      error("""Workflows :: ansible :: playbook :: Attempt to use ansible binary failed, please ensure the image [$dockerImage] contains an install of Ansible and that it is in the PATH.""")
    }
    def ansibleVersion = sh(returnStdout: true, script: 'ansible --version').split('\n')[0].split(' ')[1]
    println "Container has Ansible version [$ansibleVersion] installed."
    def ansiblePluginAvailable = new Commands().getPluginVersion('ansible')
    if (ansiblePluginAvailable) {
      ansiblePlaybook(colorized:      true,
                      credentialsId:  credential.id,
                      extras:         extras,
                      forks:          forks,
                      inventory:      inventory,
                      limit:          limit,
                      playbook:       playbook,
                      skippedTags:    skippedTags,
                      sudo:           sudo,
                      sudoUser:       sudoUser,
                      tags:           tags)
    } else {
      error("Workflows :: ansible :: playbook :: Ansible workflow currently only works with the Ansible plugin installed.")
    }
  }
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'playbook':
      def playbook = args?.playbook ?: yml.tools?.ansible?.playbook
      return "ansible: playbook: ${playbook}"
  }
}

public tests(Map yml, Map args) {
  String workflowName = 'ansible'
  println "Testing $workflowName"
}

return this;
