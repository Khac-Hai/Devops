#!/bin/bash

# Đảm bảo network quickbite-net đã tồn tại
docker network inspect quickbite-net >/dev/null 2>&1 || docker network create quickbite-net

# Khởi chạy 4 dịch vụ với Docker Compose
echo "Starting QuickBite microservices with Docker Compose..."
docker compose up -d

# Hiển thị trạng thái các container
echo ""
echo "Containers status:"
docker compose ps
