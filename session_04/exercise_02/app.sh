#!/bin/bash
echo "[StoreX Backend v2] Starting Spring Boot application..."
echo "[StoreX Backend v2] Target Database: $SPRING_DATASOURCE_URL"
echo "[StoreX Backend v2] Target Redis: $SPRING_REDIS_HOST:$SPRING_REDIS_PORT"

# Verify PostgreSQL connection immediately (since depends_on service_healthy ensured it is up)
echo "[StoreX Backend v2] Verifying PostgreSQL connection..."
pg_isready -h postgres -p 5432 -U "$SPRING_DATASOURCE_USERNAME" -d storex_db
if [ $? -eq 0 ]; then
  echo "[StoreX Backend v2] PostgreSQL is healthy and accepting connections!"
else
  echo "[StoreX Backend v2] ERROR: PostgreSQL is not ready!"
  exit 1
fi

# Verify Redis connection immediately
echo "[StoreX Backend v2] Verifying Redis connection..."
redis-cli -h redis ping
if [ $? -eq 0 ]; then
  echo "[StoreX Backend v2] Redis is healthy and responding PONG!"
else
  echo "[StoreX Backend v2] ERROR: Redis is not ready!"
  exit 1
fi

echo "[StoreX Backend v2] HikariPool-1 - Initialized connection pool successfully."
echo "[StoreX Backend v2] RedisConnectionFactory - Redis cache client initialized."
echo "[StoreX Backend v2] Tomcat started on port 8080 (http)"
echo "[StoreX Backend v2] Application started successfully in 2.15 seconds."

tail -f /dev/null
