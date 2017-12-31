import com.concur.*;

workflowDoc = '''
overview: Send an email within your pipeline.
additional_resources:
  - name: Plugin site
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
full_example:
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
          - email:
            - send: "Deployment to staging successful for branch {{ branch_name }} | {{ build_url }}"
      master:
        steps:
          - ansible:
            - playbook:
                limit: production
          - email:
            - send: "Merge to master successful, deployment successful | {{ build_url }}"
'''

concurPipeline  = new Commands()
concurUtil      = new Util()

/*
description: Execute an Ansible playbook
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
                send: "Example email from {{ build_url }}"
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

return this;
