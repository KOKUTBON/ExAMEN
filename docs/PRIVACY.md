# Privacy

ExAMEN is intended for sensitive reflection data. Self-hosting gives the operator control of the database, backups, logs, mail provider, and network boundary; it does not remove the operator's responsibility to protect them.

## What is stored

The application stores account details, hashed passwords, journal and gratitude entries, examination answers, feeling scores, todos, custom prompts, settings, notifications, derived growth metrics, and optional insight metadata in PostgreSQL. Account export includes the user's stored content as JSON. Account deletion removes the account and its dependent records after the current password is verified.

## What leaves the host

With the default configuration, reflection content is not sent to an AI provider. Gemini is disabled unless both `APP_GEMINI_ENABLED=true` and `APP_GEMINI_API_KEY` are explicitly supplied. When enabled, selected session and gratitude context is sent to Google's Gemini API. Review Google's terms and privacy documentation, and tell users before enabling this integration.

SMTP is optional. If configured, account email addresses and the contents of password-reset or reminder messages go to the configured mail provider. If SMTP is not configured, password-reset links are logged for local operator-assisted recovery; this is suitable only for development or a tightly controlled private deployment.

ExAMEN does not include third-party analytics, advertising, tracking pixels, or telemetry by default.

## Repository hygiene

Public fixtures use synthetic names, addresses, tokens, and reflection text. Do not add real journal entries, database files, exports, PDFs, screenshots containing personal data, analytics dumps, reset links, or API keys to commits, issues, tests, or documentation.

## Operator checklist

- Use HTTPS and restrict database and actuator access.
- Set unique database credentials and a randomly generated `APP_JWT_SECRET` of at least 32 UTF-8 bytes.
- Set `APP_CORS_ALLOWED_ORIGINS` to exact production origins; do not use a wildcard with credentials.
- Protect and encrypt backups, logs, and exported files; define retention and deletion procedures.
- Configure trusted SMTP if password reset or email reminders are enabled.
- Keep Gemini disabled unless the external transfer is an intentional, documented choice.
