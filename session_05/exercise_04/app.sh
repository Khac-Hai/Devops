#!/bin/bash

# Đảm bảo network quickbite-net tồn tại
docker network inspect quickbite-net >/dev/null 2>&1 || docker network create quickbite-net

echo "=========================================="
echo " Starting Stack with Hibernate DDL-Auto..."
echo "=========================================="
docker compose up -d --build

echo ""
echo "Waiting 5 seconds for PostgreSQL to initialize..."
sleep 5

echo ""
echo "=========================================="
echo " Checking tables in quickbite_user_db:"
echo "=========================================="
docker exec -it quickbite-db psql -U postgres -d quickbite_user_db -c "\dt"
