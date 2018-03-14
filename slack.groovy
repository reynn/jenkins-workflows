import com.concur.*;

workflowDoc = '''
title: Slack
overview: Send messages to a Slack channel during a job run.
additional_resources:
  - name: Slack API
    url: https://api.slack.com
  - name: Jenkins Slack plugin GitHub
    url: https://github.com/jenkinsci/slack-plugin
tools:
  - name: channel
    type: String
    description: The channel this message should go to, if a private channel the token provided should be allowed to post there.
    required: true
  - name: message
    type: String
    description: The message content that will get sent.
    required: true
  - name: credentials
    type: Map
    description: A Slack token to use while sending, should be a secret text credential type.
    required: true
  - name: teamDomain
    type: String
    description: The Slack team name
    required: false
    default: env.DEFAULT_SLACK_DOMAIN
  - name: color
    type: String
    description: Color to show on the left of the message, can use a hex code or one of (good, danger, warning).
    required: false
    default: 'good'
  - name: botUser
    type: Boolean
    description: Notification will be sent via a bot user instead of the default user specified in the token.
    required: false
    default: false
  - name: failOnError
    type: Boolean
    description: If true the entire build will fail if the Slack send is unsuccessful.
    required: false
    default: false
full_example: |
  pipelines:
    tools:
      branches:
        patterns:
          feature: .+
    tools:
      slack:
        credentials:
          description: Slack token
        channel: git-notifications
        teamDomain: concur-test
    branches:
      feature:
        steps:
          - slack:
            - send:
                message: "Job started on {{ build_url }}"
          - golang:
            - build:
          - docker:
            - build:
            - push:
'''

concurPipeline  = new Commands()
concurUtil      = new Util()
concurGit       = new Git()

/*
description: Send messages to a Slack channel during a job run.
parameters:
  - name: channel
    type: String
    description: The channel this message should go to, if a private channel the token provided should be allowed to post there.
    required: true
  - name: message
    type: String
    description: The message content that will get sent.
    required: true
  - name: credentials
    type: Map
    description: A Slack token to use while sending, should be a secret text credential type.
    required: true
  - name: teamDomain
    type: String
    description: The Slack team name
    required: false
    default: env.DEFAULT_SLACK_DOMAIN
  - name: color
    type: String
    description: Color to show on the left of the message, can use a hex code or one of (good, danger, warning).
    required: false
    default: 'good'
  - name: botUser
    type: Boolean
    description: Notification will be sent via a bot user instead of the default user specified in the token.
    required: false
    default: false
  - name: failOnError
    type: Boolean
    description: If true the entire build will fail if the Slack send is unsuccessful.
    required: false
    default: false
example: |
  branches:
    feature:
      steps:
        - slack:
            # Simple
            - send:
            # Advanced
            - send:
                message: "Test message during job #<{{ build_url }}|{{ build_number }}}>."
 */
public send(Map yml, Map args) {
  String channel      = args?.channel     ?: yml.tools?.slack?.channel
  String message      = args?.message     ?: yml.tools?.slack?.message
  Map credentials     = args?.credentials ?: yml.tools?.slack?.credentials
  String teamDomain   = args?.teamDomain  ?: yml.tools?.slack?.teamDomain   ?: env.DEFAULT_SLACK_DOMAIN
  String color        = args?.color       ?: yml.tools?.slack?.color        ?: 'good'
  Boolean botUser     = args?.botUser     ?: yml.tools?.slack?.botUser      ?: false
  Boolean failOnError = args?.failOnError ?: yml.tools?.slack?.failOnError  ?: false

  assert channel      : "Workflows :: slack :: send :: No [channel] provided in tools.slack or as a parameter to this step."
  assert message      : "Workflows :: slack :: send :: No [message] provided in tools.slack or as a parameter to this step."
  assert credentials  : "Workflows :: slack :: send :: No [credentials] provided in tools.slack or as a parameter to this step."

  def cred = concurPipeline.getCredentialsWithCriteria(credentials).id
  slackData.put('tokenCredentialId', cred)

  Map slackData = [
    'channel'           : channel,
    'message'           : message,
    'tokenCredentialId' : cred.id,
    'teamDomain'        : teamDomain,
    'color'             : color,
    'botUser'           : botUser,
    'failOnError'       : failOnError
  ]

  slackData.each {
    slackData[it.key] = concurUtil.mustacheReplaceAll(it.value)
  }

  concurPipeline.debugPrint('Workflows :: Slack :: send', slackData)

  slackSend(slackData)
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'send':
      String channel = args?.channel ?: yml.tools?.slack?.channel
      return channel ? "slack: send: ${channel}" : 'slack: send'
  }
}

public tests(Map yml, Map args) {
  String workflowName = 'slack'
  println "Testing $workflowName"
}

return this;
