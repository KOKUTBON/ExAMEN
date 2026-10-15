#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml}"
SERVICE="${SERVICE:-postgres}"
OUTPUT="${OUTPUT:-backup-$(date +%Y%m%d%H%M%S).sql}"

echo "Backing up Postgres from service '$SERVICE' to $OUTPUT"
docker compose -f "$COMPOSE_FILE" exec -T "$SERVICE" pg_dump -U ${POSTGRES_USER:-postgres} ${POSTGRES_DB:-examen} > "$OUTPUT"
echo "Backup written to $OUTPUT"
