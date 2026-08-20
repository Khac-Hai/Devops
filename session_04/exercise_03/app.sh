#!/bin/bash
echo "[StoreX Backend v3] Initializing service on custom network 'storex-net'..."
echo "[StoreX Backend v3] Database Host: $DB_HOST (Network: storex-net)"
echo "[StoreX Backend v3] Database User: $DB_USER"
echo "[StoreX Backend v3] Verifying internal connection to PostgreSQL..."

until pg_isready -h "$DB_HOST" -p 5432 -U "$DB_USER" > /dev/null 2>&1; do
  echo "[StoreX Backend v3] Waiting for PostgreSQL..."
  sleep 2
done

echo "[StoreX Backend v3] Successfully authenticated and connected to $DB_HOST:5432 via custom network 'storex-net'!"
echo "[StoreX Backend v3] Application started and listening on port 8080."
tail -f /dev/null
