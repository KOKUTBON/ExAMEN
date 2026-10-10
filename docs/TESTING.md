# Testing

Run the backend unit and integration suite with `cd examen-backend && ./gradlew clean test --no-daemon`. It covers authentication, password reset, JWT handling, session lifecycle, abuse controls, profile endpoints, ownership isolation, optional-AI fallback behavior, JSON export, and account deletion. Tests use their test profile and synthetic data.

Run frontend type checks and unit tests with `cd examen-frontend && npm ci && npm run typecheck && npm test -- --watch=false`, then compile the SSR app with `npm run build`. Browser journeys live in `examen-frontend/e2e/`; they use synthetic API fixtures and cover login, password reset, journaling, examination completion, settings, todos, categories, insights, notifications, and PDF export. Run `npm run e2e:install && npm run e2e`.

Docker validation should include `docker compose config`, `docker compose build`, starting PostgreSQL and the backend so Spring Boot applies migrations, checking `flyway_schema_history`, and running the backup/restore rehearsal. CI runs backend tests, frontend tests/build, both Docker image builds, and a PostgreSQL backup and restore rehearsal. A production release also needs a deployment check for TLS, browser API routing, credentials, migrations, mail delivery, and data retention; these are not proved by unit tests.
