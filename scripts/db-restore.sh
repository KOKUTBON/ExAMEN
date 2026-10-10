#!/usr/bin/env bash
set -euo pipefail

if [ $# -lt 1 ]; then
  echo "Usage: $0 <backup-file>"
  exit 1
fi

FILE="$1"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml}"
SERVICE="${SERVICE:-postgres}"

echo "Restoring Postgres from $FILE into service '$SERVICE'"
cat "$FILE" | docker compose -f "$COMPOSE_FILE" exec -T "$SERVICE" psql -U ${POSTGRES_USER:-postgres} ${POSTGRES_DB:-examen}
echo "Restore completed"
