# Security Policy

Examen processes account credentials and private reflection content. Security reports are handled with care and should be submitted privately.

## Supported versions

Security fixes are applied to the latest code on `main`. No numbered production release is currently maintained as a separate security branch.

## Reporting a vulnerability

Use [GitHub private vulnerability reporting](https://github.com/KOKUTBON/ExAMEN/security/advisories/new). If that channel is unavailable, contact the repository owner privately. Do not open a public issue, pull request, or discussion containing exploit details or personal data.

Please include:

- the affected endpoint, component, commit, or version;
- clear reproduction steps or a minimal proof of concept;
- expected and observed behavior;
- likely impact and required preconditions;
- suggested remediation, if known.

Remove real credentials, tokens, journal entries, and other personal data from the report. Use test accounts and synthetic examples.

## Response targets

- Initial acknowledgment: within 72 hours.
- Triage and severity assessment: within 7 days.
- Remediation and disclosure timing: based on impact, exploitability, and release complexity.

The maintainer will coordinate disclosure with the reporter when practical. Please allow a reasonable remediation period before publishing details.

## Deployment responsibilities

Self-hosting operators must provide unique JWT and database credentials, HTTPS, protected backups, restricted database access, appropriate data retention, and secure SMTP and Gemini credentials. Optional Gemini processing can send reflection context to an external service and should be disclosed to users. Sample development credentials are unsuitable for production.

## Private reporting contact

Email [kokutbon@gmail.com](mailto:kokutbon@gmail.com) with subject `[ExAMEN] Security report`. Include affected version/commit, reproduction using synthetic data, preconditions and impact. Do not include live credentials or personal data. This mailbox is the fallback while the repository is private or GitHub's reporting form is unavailable.

After publication, prefer [GitHub private vulnerability reporting](https://github.com/KOKUTBON/ExAMEN/security/advisories/new) when enabled. Acknowledgment and remediation depend on maintainer availability; no response deadline is guaranteed. Coordinate disclosure and fixes privately.
