import com.concur.*;

workflowDoc = '''
overview: Steps for building and testing with Golang.
additional_resources:
  - name: Glide
    url: https://github.com/Masterminds/glide
  - name: Godep
    url: https://github.com/tools/godep
  - name: Golang
    url: https://golang.org
  - name: Docker Images
    url: https://hub.docker.com/_/golang/
tools:
  - type: String
    name: buildImage
    section: golang
    required: true
    description: Docker image that has Golang/Glide/Godep installed.
  - type: List
    name: additionalArgs
    section: glide
    description: Any additional arguments to Glide as a YAML style List.
  - type: String
    name: command
    section: glide
    default: install
    description: Which Glide command to run.
  - type: List
    name: additionalArgs
    section: godep
    description: Any additional arguments to Godep as a YAML style List.
  - type: String
    name: command
    section: godep
    default: restore
    description: Which Godep command to run.
  - type: String
    name: goPath
    section: golang
    default: determined by SCM
    description: The path within the container to mount the project into.
  - type: String
    name: outFile
    section: golang
    description: Where the built Go binary will be put instead of the current directory.
  - type: Map
    name: env
    section: golang
    description: Setup for the build environment, for example setting GOOS or GOARCH.
  - type: List
    name: additionalArgs
    section: golang
    description: Any additional arguments to `go build` as a YAML style List.
  - type: String
    name: mainPath
    section: golang
    description: Path to the main .go file to build.
  - type: List
    name: additionalArgs
    section: test
    description: Additional arguments to the test binary specified.
  - type: String
    name: binary
    section: test
    default: go test
    description: The binary to use for the test, in case a different framework is being used.
  - type: String
    name: resultsPath
    section: test
    default: test_results
    description: If a test framework, such as Gingko, that can output to Junit is being used this is the path to the directory.
  - type: Boolean
    section: golang
    name: gatherJunit
    default: false
    description: If a test framework, such as Gingko, that can output to Junit this will ensure that the test results are published in Jenkins.
  - type: String
    name: junitPattern
    section: golang
    default: ${resultsPath}/*.xml
    description: An ant style pattern for the junit plugin, should match where your test results get stored.
full_example: |
  pipelines:
    tools:
      branches:
        patterns:
          feature: .+
    branches:
      feature:
        steps:
          - golang:
            - glide:
            - test:
                binary: ginkgo
                gatherJunit: true
            - build:
                mainPath: cmd/app/main.go
                outFile: publish/app
                env:
                  GOOS: darwin
                  GOARCH: amd64
'''

concurPipeline  = new Commands()
concurGit       = new Git()
concurUtil      = new Util()

/*
description: Vendor Package Management for your Go projects.
parameters:
  - type: String
    name: buildImage
    required: true
    description: Docker image that has Glide installed.
  - type: List
    name: additionalArgs
    description: Any additional arguments to Glide as a YAML style List.
  - type: String
    name: command
    default: install
    description: Which Glide command to run.
  - type: String
    name: goPath
    default: determined by SCM
    description: The path within the container to mount the project into.
example:
  branches:
    feature:
      steps:
        - golang:
            # Simple
            - glide:
            # Advanced
            - glide:
                command: install
                additionalArgs:
                  - "--force"
 */
public glide(Map yml, Map args) {
  String dockerImage  = args?.buildImage      ?: yml.tools?.golang?.buildImage
  List additionalArgs = args?.additionalArgs  ?: yml.tools?.glide?.additionalArgs
  String command      = args?.command         ?: yml.tools?.glide?.command        ?: "install"
  String goPath       = args?.goPath          ?: yml.tools?.golang?.goPath        ?: getGoPath()

  assert goPath      : "Workflows :: Golang :: glide :: [goPath] is required in [tools.golang] or as a parameter to the test step."
  assert dockerImage : "Workflows :: Golang :: glide :: [buildImage] is needed in [tools.golang] or as a parameter to the test step."

  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)

  def glideCommand = "glide ${command}"

  /**
   * Define additional args like the following
   * ----------------------------------------------
   * - golang:
   *   - glide:
   *       additionalArgs:
   *         - "--force"
   *         - "--skip-test"
   *         - "-v"
   */
  if (additionalArgs) {
    glideCommand = "$glideCommand ${additionalArgs.join(' ')}"
  }

  glideCommand = concurUtil.mustacheReplaceAll(glideCommand)

  concurPipeline.debugPrint('Workflows :: golang :: glide', [
    'dockerImage'     : dockerImage,
    'goPath'          : goPath,
    'command'         : command,
    'additionalArgs'  : additionalArgs,
    'glideCommand'    : glideCommand
  ])

  // -u 0:0 runs as root, -v mounts the current workspace to your gopath
  runCommandInDockerImage(dockerImage, goPath, {
    concurUtil.installGoPkg('glide', 'github.com/Masterminds/glide')
    sh "cd ${goPath} && ${glideCommand}"
  })
}

/*
description: Godep is a tool for managing Go package dependencies.
parameters:
  - type: String
    name: buildImage
    required: true
    description: Docker image that has Godep installed.
  - type: List
    name: additionalArgs
    description: Any additional arguments to Godep as a YAML style List.
  - type: String
    name: command
    default: restore
    description: Which Godep command to run.
  - type: String
    name: goPath
    default: determined by SCM
    description: The path within the container to mount the project into.
example:
  branches:
    feature:
      steps:
        - golang:
            # Simple
            - godep:
            # Advanced
            - godep:
                command: update
                additionalArgs:
                  - "-v"
                  - "-goversion"
 */
public godep(Map yml, Map args) {
  String dockerImage  = args?.buildImage      ?: yml.tools?.golang?.buildImage
  List additionalArgs = args?.additionalArgs  ?: yml.tools?.godep?.additionalArgs
  String command      = args?.command         ?: yml.tools?.godep?.command        ?: "restore"
  String goPath       = args?.goPath          ?: yml.tools?.golang?.goPath        ?: getGoPath()

  assert goPath      : "Workflows :: Golang :: godep :: [goPath] is required in [tools.golang] or as a parameter to the test step."
  assert dockerImage : "Workflows :: Golang :: godep :: [buildImage] is needed in [tools.golang] or as a parameter to the test step."

  def godepCommand = "godep ${command}"
  /**
   * Define additional args as any of the following
   * ----------------------------------------------
   * - golang:
   *   - godep:
   *       additionalArgs:
   *         - "--force"
   *         - "--skip-test"
   *         - "-v"
   */
  if (additionalArgs) {
    godepCommand = "$godepCommand ${additionalArgs.join(' ')}"
  }

  godepCommand = concurUtil.mustacheReplaceAll(godepCommand)

  concurPipeline.debugPrint('Workflows :: golang :: godep', [
    'dockerImage'     : dockerImage,
    'goPath'          : goPath,
    'command'         : command,
    'additionalArgs'  : additionalArgs,
    'godepCommand'    : godepCommand
  ])

  runCommandInDockerImage(dockerImage, goPath, {
    concurUtil.installGoPkg('godep', 'github.com/tools/godep')
    sh "cd ${goPath} && ${godepCommand}"
  })
}

/*
description: Dep is a tool for managing Go package dependencies.
parameters:
  - type: String
    name: buildImage
    required: true
    description: Docker image that has Godep installed.
  - type: List
    name: additionalArgs
    description: Any additional arguments to Godep as a YAML style List.
  - type: String
    name: command
    default: restore
    description: Which Godep command to run.
  - type: String
    name: goPath
    default: determined by SCM
    description: The path within the container to mount the project into.
example:
  branches:
    feature:
      steps:
        - golang:
            # Simple
            - dep:
            # Advanced
            - dep:
                additionalArgs:
                  - "-v"
                  - "-update"
 */
public dep(Map yml, Map args) {
  String dockerImage  = args?.buildImage      ?: yml.tools?.golang?.buildImage
  List additionalArgs = args?.additionalArgs  ?: yml.tools?.dep?.additionalArgs
  String command      = args?.command         ?: yml.tools?.dep?.command        ?: "ensure"
  String goPath       = args?.goPath          ?: yml.tools?.golang?.goPath      ?: getGoPath()

  assert goPath      : "Workflows :: Golang :: dep :: [goPath] is required in [tools.golang] or as a parameter to the test step."
  assert dockerImage : "Workflows :: Golang :: dep :: [buildImage] is needed in [tools.golang] or as a parameter to the test step."

  def depCommand = "dep ${command}"
  /**
   * Define additional args as any of the following
   * ----------------------------------------------
   * - golang:
   *   - dep:
   *       additionalArgs:
   *         - "--force"
   *         - "--skip-test"
   *         - "-v"
   */
  if (additionalArgs) {
    depCommand = "$depCommand ${additionalArgs.join(' ')}"
  }

  depCommand = concurUtil.mustacheReplaceAll(depCommand)

  concurPipeline.debugPrint('Workflows :: golang :: dep', [
    'dockerImage'     : dockerImage,
    'goPath'          : goPath,
    'command'         : command,
    'additionalArgs'  : additionalArgs,
    'depCommand'    : depCommand
  ])

  runCommandInDockerImage(dockerImage, goPath, {
    concurUtil.installGoPkg('dep', 'github.com/tools/dep')
    sh "cd ${goPath} && ${depCommand}"
  })
}

/*
description: Build a Golang project.
parameters:
  - type: String
    name: buildImage
    description: Docker image that has the linting tool installed.
  - type: List
    name: additionalFlags
    description: Any additional arguments to the linting tool as a YAML style List.
  - type: List
    name: enable
    description: A list of linters to enable.
    default: []
  - type: String
    name: binary
    description: The binary you want to use for linting.
    default: gometalinter
  - type: String
    name: goPath
    description: The path within the container to mount the project into.
    default: getGoPath()
example:
  branches:
    feature:
      steps:
        - golang:
            # Simple
            - lint:
            # Advanced
            - lint:
                binary: gometalinter.v1
                enable:
                  - vet
                  - deadcode
                  - goconst
                  - errcheck
                  - goimports
                additionalFlags:
                  - tests
 */
public lint(Map yml, Map args) {
  String dockerImage    = args?.buildImage      ?: yml.tools?.golang?.buildImage
  List additionalFlags  = args?.additionalFlags ?: yml.tools?.lint?.additionalFlags
  List enable           = args?.enable          ?: yml.tools?.lint?.enable          ?: []
  String binary         = args?.binary          ?: yml.tools?.lint?.binary          ?: 'gometalinter'
  String installer      = args?.installer       ?: yml.tools?.lint?.installer       ?: 'github.com/alecthomas/gometalinter'
  String goPath         = args?.goPath          ?: yml.tools?.golang?.goPath        ?: getGoPath()

  String lintCommand = binary

  if (enable.size() > 0) {
    lintCommand = "$lintCommand --disable-all ${enable.collect { "--enable=$it" }.join(' ')}"
  }

  if (additionalFlags) {
    lintCommand = "$lintCommand ${additionalFlags.collect { "--$it" }.join(' ')}"
  }

  concurPipeline.debugPrint('Workflows :: Golang :: Lint', [
    'dockerImage'     : dockerImage,
    'additionalFlags' : additionalFlags,
    'enable'          : enable,
    'binary'          : binary,
    'installer'       : installer,
    'goPath'          : goPath
  ])

  runCommandInDockerImage(dockerImage, goPath, {
    concurUtil.installGoPkg(binary, installer)
    try {
      sh "$binary --install"
    } catch (e) { error("Failed to install linters for $binary") }

    if (additionalFlags.find { it == 'checkstyle' }) {
      def lintResults = sh returnStdout: true, script: "cd ${goPath} && ${lintCommand}"
      writeFile file: 'checkstyle.xml', text: lintResults
      concurPipeline.debugPrint('Workflows :: Golang :: Lint', 'Wrote checkstyle.xml file.')
      if (concurPipeline.getgetPluginVersion('checkstyle')) {
        concurPipeline.debugPrint('Workflows :: Golang :: Lint', 'Checkstyle plugin installed, calling plugin.')
        checkstyle canComputeNew: false, canRunOnFailed: true, defaultEncoding: '', healthy: '', pattern: 'checkstyle.xml', unHealthy: ''
      }
    } else {
      sh "cd ${goPath} && ${lintCommand}"
    }
  })
}

/*
description: Build a Golang project.
parameters:
  - type: String
    name: buildImage
    required: true
    description: Docker image that has any Golang installed.
  - type: String
    name: goPath
    default: determined by SCM
    description: The path within the container to mount the project into.
  - type: String
    name: outFile
    section: golang
    description: Where the built Go binary will be put instead of the current directory.
  - type: Map
    name: env
    section: golang
    description: Setup for the build environment, for example setting GOOS or GOARCH.
  - type: String
    name: mainPath
    section: golang
    description: Path to the main .go file to build.
example:
  branches:
    feature:
      steps:
        - golang:
            # Simple
            - build:
            # Advanced
            - build:
                outFile: "publish/example-binary"
                mainPath: "cmd/app/main.go"
                env:
                  GOOS: linux
                  GOARCH: amd64
 */
public build(Map yml, Map args) {
  String dockerImage  = args?.buildImage      ?: yml.tools?.golang?.buildImage
  String outFile      = args?.outFile         ?: yml.tools?.golang?.outFile
  Map goEnv           = args?.env             ?: yml.tools?.golang?.env
  String mainPath     = args?.mainPath        ?: yml.tools?.golang?.mainPath
  List additionalArgs = args?.additionalArgs  ?: yml.tools?.golang?.additionalArgs
  String goPath       = args?.goPath          ?: yml.tools?.golang?.goPath          ?: getGoPath()

  assert goPath      : "Workflows :: Golang :: build :: [goPath] is required in [tools.golang] or as a parameter to the test step."
  assert dockerImage : "Workflows :: Golang :: build :: [buildImage] is needed in [tools.golang] or as a parameter to the test step."

  def goCommand = "go build"

  if (goEnv) {
    def envStr = goEnv.collect { "${it.key}=${it.value}" }.join(' ')
    goCommand = "${envStr} ${goCommand}"
  }

  if (outFile) {
    goCommand = "${goCommand} -o ${outFile}"
  }

  /**
   * Define additional args as any of the following
   * ----------------------------------------------
   * - golang:
   *   - build:
   *       additionalArgs:
   *         - "-a"
   *         - "-n"
   *         - "-p 5"
   * ----------------------------------------------
   */
  if (additionalArgs) {
    goCommand = "${goCommand} ${additionalArgs.join(' ')}"
  }

  if (mainPath) {
    goCommand = "${goCommand} ${mainPath}"
  }

  goCommand = concurUtil.mustacheReplaceAll(goCommand)

  concurPipeline.debugPrint('Workflow :: golang :: build', [
    'dockerImage'   : dockerImage,
    'goPath'        : goPath,
    'outFile'       : outFile,
    'goEnv'         : goEnv,
    'additionalArgs': additionalArgs,
    'goCommand'     : goCommand
  ])

  runCommandInDockerImage(dockerImage, goPath, {
    sh "cd ${goPath} && ${goCommand}"
  })
}

/*
description: Build a Golang project.
parameters:
  - type: String
    name: buildImage
    required: true
    description: Docker image that has any Golang installed.
  - type: String
    name: goPath
    default: determined by SCM
    description: The path within the container to mount the project into.
  - type: List
    name: additionalArgs
    description: Any additional arguments to Glide as a YAML style List.
  - type: String
    name: binary
    default: go test
    description: The binary to use for the test, in case a different framework is being used.
  - type: String
    name: resultsPath
    default: test_results
    description: If a test framework, such as Gingko, that can output to Junit is being used this is the path to the directory.
  - type: Boolean
    name: gatherJunit
    default: false
    description: If a test framework, such as Gingko, that can output to Junit this will ensure that the test results are published in Jenkins.
  - type: String
    name: junitPattern
    default: ${resultsPath}/*.xml
    description: An ant style pattern for the junit plugin, should match where your test results get stored.
example:
  branches:
    feature:
      steps:
        - golang:
            # Simple
            - test:
            # Advanced
            - test:
                binary: ginkgo
                additionalArgs:
                  - "./..."
                gatherJunit: true
                resultsPath: results
 */
public test(Map yml, Map args) {
  String dockerImage  = args?.buildImage      ?: yml.tools?.golang?.buildImage
  List additionalArgs = args?.additionalArgs  ?: yml.tools?.golang?.test?.additionalArgs
  String goPath       = args?.goPath          ?: yml.tools?.golang?.goPath            ?: getGoPath()
  String testBinary   = args?.binary          ?: yml.tools?.golang?.test?.binary      ?: "go test"
  String resultsPath  = args?.resultsPath     ?: yml.tools?.golang?.test?.resultsPath ?: "test_results"
  Boolean gatherJunit = args?.gatherJunit     ?: yml.tools?.golang?.gatherJunit       ?: false
  String junitPattern = args?.junitPattern    ?: yml.tools?.golang?.junitPattern      ?: "${resultsPath}/*.xml"

  assert dockerImage  : "Workflows :: Golang :: [buildImage] is needed in [tools.golang] or as a parameter to the test step."
  assert goPath       : "Workflows :: Golang :: [goPath] is required in [tools.golang] or as a parameter to the test step."
  assert junitPattern : "Workflows :: Golang :: [junitPattern] is required in [tools.golang] or as a parameter to the test step."

  String testCommand = testBinary

  /**
   * Define additional args as any of the following
   * ----------------------------------------------
   * - golang:
   *   - test:
   *       additionalArgs:
   *         - "-a"
   *         - "-n"
   *         - "-p 5"
   */
  if (additionalArgs) {
    testCommand = "${testCommand} ${additionalArgs.join(' ')}"
  }

  def shCmd = "cd ${goPath} && mkdir -p ${goPath}/${resultsPath} && ${testCommand}"

  shCmd = concurUtil.mustacheReplaceAll(shCmd)

  concurPipeline.debugPrint('Workflows :: golang :: test', [
    'dockerImage'   : dockerImage,
    'goPath'        : goPath,
    'testBinary'    : testBinary,
    'additionalArgs': additionalArgs,
    'resultsPath'   : resultsPath,
    'gatherJunit'   : gatherJunit,
    'junitPattern'  : junitPattern,
    'testCommand'   : testCommand,
    'shCmd'         : shCmd
  ])

  runCommandInDockerImage(dockerImage, goPath, {
    sh shCmd
  })
  if (gatherJunit) {
    junit junitPattern
  }
}

private getGoPath() {
  return "/go/src/${GIT_HOST}/${env.GIT_OWNER}/${env.GIT_REPO}"
}

private runCommandInDockerImage(String dockerImage, String goPath, Closure work) {
  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)
  docker.image(dockerImage).inside("-u 0:0 -v ${pwd()}:${goPath}") {
    work()
  }
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'glide':
      String glideCommand = args?.command ?: "install"
      return "golang: glide: $glideCommand"
    case 'godep':
      String godepCommand = args?.command ?: "ensure"
      return "golang: godep: $godepCommand"
    case 'lint':
      String binary = args?.binary ?: yml.tools?.lint?.binary ?: "gometalinter"
      return "golang: $binary"
    case 'build':
      String os    = args?.env?.GOOS   ?: yml.tools?.golang?.env?.GOOS
      String arch  = args?.env?.GOARCH ?: yml.tools?.golang?.env?.GOARCH
      return os ? arch ? "golang: build: $arch/$os" : "golang: build: $os" : 'golang: build'
    case 'test':
      List testCommand = args?.additionalArgs
      return testCommand ? "golang: test: ${testCommand}" : 'golang: test'
  }
}

return this;
