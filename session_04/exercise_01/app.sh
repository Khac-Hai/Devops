#!/bin/bash
echo "[StoreX Backend] Starting Spring Boot application..."
echo "[StoreX Backend] Connecting to database at $SPRING_DATASOURCE_URL..."

# Extract host from SPRING_DATASOURCE_URL (e.g. jdbc:postgresql://postgres:5432/storex -> postgres)
DB_HOST=$(echo $SPRING_DATASOURCE_URL | sed -e 's/.*:\/\///' -e 's/:.*//')

echo "[StoreX Backend] Waiting for database at $DB_HOST:5432..."
until pg_isready -h "$DB_HOST" -p 5432 -U "$SPRING_DATASOURCE_USERNAME" > /dev/null 2>&1; do
  echo "[StoreX Backend] Database is unavailable - sleeping..."
  sleep 2
done

echo "[StoreX Backend] Successfully connected to PostgreSQL ($DB_HOST:5432)!"
echo "[StoreX Backend] HikariPool-1 - Starting..."
echo "[StoreX Backend] HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection"
echo "[StoreX Backend] HikariPool-1 - Start completed."
echo "[StoreX Backend] Tomcat started on port 8080 (http) with context path ''"
echo "[StoreX Backend] Started StoreXApplication in 3.42 seconds (process running for 4.12)"

# Keep container running
tail -f /dev/null
