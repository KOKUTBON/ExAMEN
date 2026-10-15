# Deployment

The Compose files provide a starting point for a private deployment. They do not replace host hardening, TLS termination, firewall rules, backups, or secret management.

## Production checklist

1. Provide unique `POSTGRES_USER` and `POSTGRES_PASSWORD` values through a secret manager or protected environment file.
2. Generate a unique `APP_JWT_SECRET` with at least 32 UTF-8 bytes.
3. Set `APP_PUBLIC_URL`, `APP_CORS_ALLOWED_ORIGINS`, and `PUBLIC_API_URL` for the real HTTPS origin.
4. Put the frontend and API behind HTTPS. Expose PostgreSQL only on the private container network.
5. Restrict `/actuator` and API documentation access if they are not needed publicly.
6. Configure SMTP with a sender address and TLS appropriate to the provider before enabling email flows.
7. Run Flyway migrations as part of the release and rehearse backups and restores before accepting personal data.
8. Decide whether optional Gemini processing is acceptable, then explicitly enable and disclose it only if so.

## Migrations and backups

The backend runs Flyway and validates the schema at startup. Apply migrations by starting the backend against the target database; inspect `flyway_schema_history` in PostgreSQL to confirm the applied versions. The scripts in `scripts/` use `pg_dump` and `pg_restore`; backup files are ignored by Git and must be stored securely.

## Reverse proxy

`deploy/nginx.conf` contains the local reverse-proxy starting point. Review upstream addresses, TLS settings, headers, body limits, and access logging for the chosen host before using it in production.
