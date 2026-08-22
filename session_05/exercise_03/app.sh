#!/bin/bash

# Đảm bảo network quickbite-net tồn tại
docker network inspect quickbite-net >/dev/null 2>&1 || docker network create quickbite-net

# 1. Kiểm tra cú pháp và giá trị nội suy của docker compose
echo "=========================================="
echo " Validating Docker Compose Interpolation:"
echo "=========================================="
docker compose config

# 2. Khởi chạy hệ thống
echo ""
echo "=========================================="
echo " Starting QuickBite Stack with .env config:"
echo "=========================================="
docker compose up -d

# 3. Hiển thị trạng thái các container
echo ""
docker compose ps
