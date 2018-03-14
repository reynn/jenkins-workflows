# Slack

## Overview

> Send messages to a Slack channel during a job run.

## Tools Section

| Name        | Required   | Type    | Default                    | Description                                                                                                     |
|:------------|:-----------|:--------|:---------------------------|:----------------------------------------------------------------------------------------------------------------|
| channel     | Required   | String  |                            | The channel this message should go to, if a private channel the token provided should be allowed to post there. |
| message     | Required   | String  |                            | The message content that will get sent.                                                                         |
| credentials | Required   | Map     |                            | A Slack token to use while sending, should be a secret text credential type.                                    |
| teamDomain  | Optional   | String  | `env.DEFAULT_SLACK_DOMAIN` | The Slack team name                                                                                             |
| color       | Optional   | String  | `good`                     | Color to show on the left of the message, can use a hex code or one of (good, danger, warning).                 |
| botUser     | Optional   | Boolean | `False`                    | Notification will be sent via a bot user instead of the default user specified in the token.                    |
| failOnError | Optional   | Boolean | `False`                    | If true the entire build will fail if the Slack send is unsuccessful.                                           |

## Available Methods

### send

> Send messages to a Slack channel during a job run.

| Name        | Required   | Type    | Default                    | Description                                                                                                     |
|:------------|:-----------|:--------|:---------------------------|:----------------------------------------------------------------------------------------------------------------|
| channel     | Required   | String  |                            | The channel this message should go to, if a private channel the token provided should be allowed to post there. |
| message     | Required   | String  |                            | The message content that will get sent.                                                                         |
| credentials | Required   | Map     |                            | A Slack token to use while sending, should be a secret text credential type.                                    |
| teamDomain  | Optional   | String  | `env.DEFAULT_SLACK_DOMAIN` | The Slack team name                                                                                             |
| color       | Optional   | String  | `good`                     | Color to show on the left of the message, can use a hex code or one of (good, danger, warning).                 |
| botUser     | Optional   | Boolean | `False`                    | Notification will be sent via a bot user instead of the default user specified in the token.                    |
| failOnError | Optional   | Boolean | `False`                    | If true the entire build will fail if the Slack send is unsuccessful.                                           |

### send Example

```yaml
branches:
  feature:
    steps:
      - slack:
          # Simple
          - send:
          # Advanced
          - send:
              message: "Test message during job #<{{ build_url }}|{{ build_number }}}>."
```

### tests

## Full Example Pipeline

```yaml
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
```

## Additional Resources

* [Slack API](https://api.slack.com)
* [Jenkins Slack plugin GitHub](https://github.com/jenkinsci/slack-plugin)