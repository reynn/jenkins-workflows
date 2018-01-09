concurPipeline  = new com.concur.ConcurCommands()
concurUtil      = new com.concur.Util()

workflowDoc = '''
overview: Execute any Gradle task.
additional_resources:
  - name: Gradle Official
    url: https://gradle.org
  - name: Docker images
    url: https://hub.docker.com/_/gradle/
tools:
  - type: String
    name: buildImage
    description: Docker image containing Gradle and any other necessary tools for the project to build.
  - type: String
    name: binary
    description: The Gradle binary to use, typically this would be `gradlew` or `gradle`.
    default: ./gradlew
  - type: String
    name: task
    description: The name of the task to execute, multiple tasks can be separated by a space.
    default: build
  - type: List
    name: extraArgs
    description: Any additional arguments to apply to the Gradle task.
full_example: |
  pipelines:
    tools:
      branches:
        patterns:
          feature: .+
    tools:
      gradle:
        buildImage: gradle:4.4-jdk9
    branches:
      feature:
        steps:
          - gradle:
            - task:
                binary: gradle
                task: "test build publish"
'''

/*
description: Execute Gradle tasks.
parameters:
  - type: String
    name: buildImage
    description: Docker image containing Gradle and any other necessary tools for the project to build.
  - type: String
    name: binary
    description: The Gradle binary to use, typically this would be `gradlew` or `gradle`.
    default: ./gradlew
  - type: String
    name: name
    description: The name of the task to execute, multiple tasks can be separated by a space.
    default: build
  - type: List
    name: extraArgs
    description: Any additional arguments to apply to the Gradle task.
example:
  branches:
    feature:
      steps:
        - gradle:
            # Simple
            - task:
            # Advanced
            - task:
                binary: gradle
                name: compile
 */
public task(Map yml, Map args) {
  String dockerImage  = args?.buildImage  ?: yml.tools?.gradle?.buildImage
  String gradleBinary = args?.binary      ?: yml.tools?.gradle?.binary    ?: './gradlew'
  String taskName     = args?.name        ?: yml.tools?.gradle?.task      ?: "build"
  List taskArgs       = args?.extraArgs   ?: yml.tools?.gradle?.extraArgs

  gradleTask = "${binary} ${taskName}"
  if (extraArgs) {
    gradleTask = "${gradleTask} ${extraArgs.join(" ")}"
  }

  assert dockerImage  : "Workflows :: gradle :: task :: No [buildImage] provided in [tools.gradle] or as a parameter to the gradle.task step."
  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)
  gradleTask  = concurUtil.mustacheReplaceAll(gradleTask)

  concurPipeline.debugPrint('Workflows :: gradle :: task', [
    'args'        : args,
    'dockerImage' : dockerImage,
    'gradleBinary': gradleBinary,
    'taskArgs'    : taskArgs
  ])

  if(gradleTask) {
    docker.image(dockerImage).inside {
      sh gradleTask
    }
  }
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'task':
      def gradleCommand = args?.name ?: yml.tools?.gradle?.task ?: "build"
      return "gradle: task: ${gradleCommand}"
  }
}

return this;
