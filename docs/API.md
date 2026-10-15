# API and configuration notes

The Spring Boot API is rooted at `/api`. Protected requests use `Authorization: Bearer <JWT>`; the frontend stores the token locally and the backend remains stateless. OpenAPI and Swagger UI are available from the backend when enabled by the deployment.

## Account and privacy endpoints

- `POST /api/auth/register`, `POST /api/auth/login`
- `POST /api/auth/reset/request`, `POST /api/auth/reset/confirm`
- `GET /api/users/me/export` — JSON export of the current account
- `POST /api/users/me/delete` — permanent deletion after password verification

## Security behavior

- JWT signing requires `APP_JWT_SECRET` with at least 32 UTF-8 bytes.
- Reset requests return an account-enumeration-safe response for unknown addresses and are rate-limited per account and time window.
- Domain services scope reads and writes to the authenticated user; integration tests cover cross-account access.
- CORS uses an explicit configured origin list. CSRF is disabled because the API uses stateless bearer tokens rather than browser cookies; do not change the client to cookie authentication without adding CSRF protection.
- Gemini calls are made only by the optional configured client; otherwise local fallback insights are used.

See `.env.example` for the complete environment variable reference.
