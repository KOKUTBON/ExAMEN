# Contributing to Examen

Thank you for improving Examen. Contributions should preserve the application's reflective purpose, user privacy, and existing workflows.

## Prerequisites
- Java 25+
- Node 20+
- Docker (optional)
- PostgreSQL 15 (or Docker Compose)

## Local setup
1. Use `.env.example` as a variable reference. Docker Compose reads a local `.env`; for `bootRun`, export variables in the shell. Never commit real credentials.
2. Set `APP_JWT_SECRET` to a random value of at least 32 bytes, then start the backend:
```bash
cd examen-backend
./gradlew bootRun
```
3. Start the frontend:
```bash
cd examen-frontend
npm install
npm start
```

## Branch and commit style
- Create short-lived branches from `main`.
- Prefer branch names in one of these forms:
  - `feat/<topic>`
  - `fix/<topic>`
  - `docs/<topic>`
- Use clear commit messages, e.g.:
  - `feat(profile): add weekly trend endpoint`
  - `fix(auth): enforce strong password on reset`

## Pull request expectations
- Keep PRs focused and scoped.
- Include a summary, rationale, and validation steps.
- Link relevant issues (`Closes #<id>` when applicable).
- Ensure CI passes before merge.
- Explain changes to stored personal data, external integrations, authentication, or authorization.
- Update public documentation when configuration or user-visible behavior changes.

## Testing checklist

Backend:
```bash
cd examen-backend
./gradlew test
```

Frontend:
```bash
cd examen-frontend
npm test -- --watch=false
npm run build
```

## Coding standards
- Favor explicit domain naming over generic utility abstractions.
- Keep controller logic thin; move business rules to services.
- Use `ApiException` for business/HTTP-aware errors.
- Add or update tests for behavior changes.
- Preserve backward-compatible API behavior unless a change is intentional and documented.

## Security and secrets
- Never commit secrets or production credentials.
- Use environment variables for JWT secrets, DB credentials, SMTP credentials, and API URLs.
- If a vulnerability is discovered, use private disclosure through repository security reporting.

## Contributor license

By submitting a contribution, you represent that you have the right to submit it and agree that it may be distributed under the repository's license at the time it is accepted. Identify third-party code or assets and include every required notice. Do not submit content copied from a source whose license is unknown or incompatible.

## Conduct
By participating, you agree to abide by the [Code of Conduct](CODE_OF_CONDUCT.md).

## Community participation

Follow the [communication guidelines](COMMUNICATION.md) and [organizational governance](GOVERNANCE.md). Keep security and conduct reports confidential and use a verified private channel; never include sensitive details in a public issue. Maintainers verify the reporting contacts and channels listed in the [readiness checklist](RELEASE_CHECKLIST.md).

## KOKUTBON contribution principles

Contribute under the existing **MIT** license in [LICENSE](LICENSE). You retain copyright in your work. Submit only work you are entitled to share, identify third-party sources and preserve their notices. No copyright assignment or paid membership is required.

Open an issue for substantial proposals, then submit a focused pull request from a branch or fork. Describe the problem, implementation, relevant checks and any privacy, accessibility, migration or safety impact. Use synthetic fixtures and sanitized logs. Maintainer review and passing relevant CI checks are expected before merging. A sole maintainer may merge their own change after checks pass and must record the rationale; no fictitious second reviewer is required.

Small documentation, translation, accessibility and reproducibility improvements are welcome. Funding does not determine review priority. Follow [communication guidelines](COMMUNICATION.md) and [the sponsorship policy](SPONSORSHIP.md).
