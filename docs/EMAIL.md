# Email

## Overview

> Send an email within your pipeline.

## Tools Section

| Name     | Required   | Type   | Default       | Description                          |
|:---------|:-----------|:-------|:--------------|:-------------------------------------|
| to       | Required   | String |               | The address to send this email to.   |
| subject  | Required   | String |               | The subject of the email.            |
| body     | Required   | String |               | The body of the email to send.       |
| from     |            | String |               | Who to show the email was sent from. |
| bcc      |            | String |               | BCC email address list.              |
| cc       |            | String |               | CC email address list.               |
| charset  |            | String | `UTF-8`       | Email body character encoding.       |
| mimeType |            | String | `text/plain.` | Email body MIME type.                |
| replyTo  |            | String |               | Reply-To email address.              |

## Available Methods

### send

> Send an email.

| Name     | Required   | Type   | Default       | Description                          |
|:---------|:-----------|:-------|:--------------|:-------------------------------------|
| to       | Required   | String |               | The address to send this email to.   |
| subject  | Required   | String |               | The subject of the email.            |
| body     | Required   | String |               | The body of the email to send.       |
| from     |            | String |               | Who to show the email was sent from. |
| bcc      |            | String |               | BCC email address list.              |
| cc       |            | String |               | CC email address list.               |
| charset  |            | String | `UTF-8`       | Email body character encoding.       |
| mimeType |            | String | `text/plain.` | Email body MIME type.                |
| replyTo  |            | String |               | Reply-To email address.              |

### send Example

```yaml
branches:
  feature:
    steps:
    - email:
      - send: Example email from {{ build_url }}
      - send:
          body: Example email from {{ build_url }}
          to: user@example.com
```

### tests

## Full Example Pipeline

```yaml
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
```

## Additional Resources

* [Mail plugin site](https://plugins.jenkins.io/workflow-basic-steps)