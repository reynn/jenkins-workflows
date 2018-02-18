import com.concur.*;

workflowDoc = '''
overview: Send an email within your pipeline.
additional_resources:
  - name: Mail plugin site
    url: https://plugins.jenkins.io/workflow-basic-steps
tools:
  - type: String
    name: to
    required: true
    description: The address to send this email to.
  - type: String
    name: subject
    required: true
    description: The subject of the email.
  - type: String
    name: body
    required: true
    description: The body of the email to send.
  - type: String
    name: from
    description: Who to show the email was sent from.
  - type: String
    name: bcc
    description: BCC email address list.
  - type: String
    name: cc
    description: CC email address list.
  - type: String
    name: charset
    description: Email body character encoding.
    default: UTF-8
  - type: String
    name: mimeType
    description: Email body MIME type.
    default: text/plain.
  - type: String
    name: replyTo
    description: Reply-To email address.
full_example: |
  pipelines:
    tools:
      email:
        cc: cc-example@domain.com
        replyTo: repyto@domain.com
      branches:
        patterns:
          feature: .+
    branches:
      feature:
        steps:
          - custom: # This should be your build process
            - buildPackage:
          - email:
            - send:
                to: team@domain.com
                body: "Deployment to staging successful for branch {{ branch_name }} | {{ build_url }}"
      master:
        steps:
          - email:
            - send:
                to: team@domain.com
                body: "Merge to master successful, deployment successful | {{ build_url }}"
'''

concurPipeline  = new Commands()
concurUtil      = new Util()

/*
description: Send an email.
parameters:
  - type: String
    name: to
    required: true
    description: The address to send this email to.
  - type: String
    name: subject
    required: true
    description: The subject of the email.
  - type: String
    name: body
    required: true
    description: The body of the email to send.
  - type: String
    name: from
    description: Who to show the email was sent from.
  - type: String
    name: bcc
    description: BCC email address list.
  - type: String
    name: cc
    description: CC email address list.
  - type: String
    name: charset
    description: Email body character encoding.
    default: UTF-8
  - type: String
    name: mimeType
    description: Email body MIME type.
    default: text/plain.
  - type: String
    name: replyTo
    description: Reply-To email address.
example:
  branches:
    feature:
      steps:
        - email:
            # Simple
            - send: "Example email from {{ build_url }}"
            # Advanced
            - send:
                to: user@example.com
                body: "Example email from {{ build_url }}"
 */
public send(Map yml, Map options) {
  def emailData = yml.tools?.email ?: [:]

  switch (options) {
    case Map:
      emailData = emailData.plus(options)
      break
    case String:
      emailData.put('body', options)
      break
    default:
      error("Workflows :: email :: send :: The format provided for email options is not supported.")
      break
  }

  assert emailData.to : "Workflows :: email :: send :: No email recipient provided."
  assert (emailData.subject || emailData.body) : "Workflows :: email :: send :: No email subject or body provided."
  if (!emailData.from) {emailData.put('from', 'buildhub@concur.com')}

  concurPipeline.debugPrint('Workflows :: email :: send', emailData)

  emailData.body    = emailData.body    ? concurUtil.mustacheReplaceAll(emailData.body) : '.'
  emailData.subject = emailData.subject ? concurUtil.mustacheReplaceAll(emailData.subject) : 'No Subject'

  mail(emailData)
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'send':
      def emailTo = args?.to ?: yml.tools?.email?.to
      return emailTo ? "email: send: ${emailTo}" : 'email: send'
  }
}

public tests(Map yml, Map args) {
  String workflowName = 'email'
  println "Testing $workflowName"
}

return this;
