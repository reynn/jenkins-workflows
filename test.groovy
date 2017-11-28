public work(yml, args) {
  println "yml :: $yml"
  println "args :: $args"
}

/*
 * Allow the Workflow execution to determine how to name each stage.
 */
public getStageName(yml, args, stepName) {
  switch(stepName) {
    case 'work':
      if (args?.name) {
        return "test: work: ${args.name}"
      } else {
        return 'test: work'
      }
  }
}

return this;
