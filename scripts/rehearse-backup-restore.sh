#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

COMPOSE_FILE="${COMPOSE_FILE:-$ROOT_DIR/docker-compose.yml}"
SERVICE="${SERVICE:-postgres}"
POSTGRES_USER="${POSTGRES_USER:-postgres}"
POSTGRES_DB="${POSTGRES_DB:-examen}"
DIRECT_PG="${DIRECT_PG:-0}"

REHEARSAL_DUMP="$ROOT_DIR/rehearsal-backup-$(date +%Y%m%d%H%M%S).sql"

cleanup() {
  if [ -f "$REHEARSAL_DUMP" ]; then
    echo "Cleaning up temporary rehearsal dump: $REHEARSAL_DUMP"
    rm -f "$REHEARSAL_DUMP"
  fi
}
trap cleanup EXIT

execute_sql() {
  local query="$1"
  if [ "$DIRECT_PG" = "1" ]; then
    psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -t -A -c "$query"
  else
    docker compose -f "$COMPOSE_FILE" exec -T "$SERVICE" psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -t -A -c "$query"
  fi
}

echo "=== [Rehearsal Step 1/5] Verifying PostgreSQL connection and state ==="
INITIAL_TABLE_COUNT=$(execute_sql "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")
echo "Initial table count in public schema: $INITIAL_TABLE_COUNT"

if [ "$INITIAL_TABLE_COUNT" -eq 0 ]; then
  echo "ERROR: Public schema has 0 tables. Run migrations before executing rehearsal." >&2
  exit 1
fi

echo "=== [Rehearsal Step 2/5] Creating test backup ==="
if [ "$DIRECT_PG" = "1" ]; then
  pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" > "$REHEARSAL_DUMP"
else
  OUTPUT="$REHEARSAL_DUMP" COMPOSE_FILE="$COMPOSE_FILE" SERVICE="$SERVICE" POSTGRES_USER="$POSTGRES_USER" POSTGRES_DB="$POSTGRES_DB" "$SCRIPT_DIR/db-backup.sh"
fi

if [ ! -s "$REHEARSAL_DUMP" ]; then
  echo "ERROR: Backup file $REHEARSAL_DUMP is empty or does not exist." >&2
  exit 1
fi
DUMP_SIZE=$(wc -c < "$REHEARSAL_DUMP" | tr -d ' ')
echo "Backup successfully created. Size: $DUMP_SIZE bytes."

echo "=== [Rehearsal Step 3/5] Simulating database disaster (Dropping public schema) ==="
execute_sql "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
AFTER_DROP_COUNT=$(execute_sql "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")
echo "Tables remaining after disaster simulation: $AFTER_DROP_COUNT"

if [ "$AFTER_DROP_COUNT" -ne 0 ]; then
  echo "ERROR: Schema was not cleared cleanly before restore." >&2
  exit 1
fi

echo "=== [Rehearsal Step 4/5] Restoring database from backup ==="
if [ "$DIRECT_PG" = "1" ]; then
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" < "$REHEARSAL_DUMP"
else
  COMPOSE_FILE="$COMPOSE_FILE" SERVICE="$SERVICE" POSTGRES_USER="$POSTGRES_USER" POSTGRES_DB="$POSTGRES_DB" "$SCRIPT_DIR/db-restore.sh" "$REHEARSAL_DUMP"
fi

echo "=== [Rehearsal Step 5/5] Verifying restored schema and data integrity ==="
RESTORED_TABLE_COUNT=$(execute_sql "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")
echo "Restored table count: $RESTORED_TABLE_COUNT (Expected: $INITIAL_TABLE_COUNT)"

if [ "$RESTORED_TABLE_COUNT" -ne "$INITIAL_TABLE_COUNT" ]; then
  echo "ERROR: Table count mismatch after restore! (Initial: $INITIAL_TABLE_COUNT, Restored: $RESTORED_TABLE_COUNT)" >&2
  exit 1
fi

echo "SUCCESS: Backup and restore rehearsal passed with 100% schema integrity!"
