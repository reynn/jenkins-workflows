import com.concur.*;

concurPipeline = new Commands()

failedTests = []
passedTests = []

public all(Map yml, Map args) {
  def groovyFiles = findFiles glob: '*.groovy'
  Map runTests = [:]

  groovyFiles.each { groovyFile ->
    runTests["Workflow : ${groovyFile.path}"] = {
      def f = groovyFile.path
      try {
        def loadedFile = load f
        loadedFile.tests(yml, args)
      } catch(e) {
        failedTests.add(f)
      }
    }
  }

  parallel runTests

  if (failedTests) {
    error("${Constants.Colors.RED}Failed tests: ${failedTests.join(',')}${Constants.Colors.CLEAR}")
  }
}

return this;
