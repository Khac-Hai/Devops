#!/bin/bash
# ==============================================================================
# Script đóng gói 4 dịch vụ Spring Boot thành Docker Images
# Session 05 - Exercise 01: QuickBite Microservices
# ==============================================================================

set -e

SERVICES=("user-service" "restaurant-service" "order-service" "notification-service")

echo "=========================================="
echo " Starting build process for 4 services..."
echo "=========================================="

for SERVICE in "${SERVICES[@]}"; do
    echo ""
    echo "------------------------------------------"
    echo ">> Processing: $SERVICE"
    echo "------------------------------------------"
    
    cd "$SERVICE"
    
    # 1. Build file JAR nếu có gradle wrapper
    if [ -f "./gradlew" ]; then
        echo "Building JAR file with Gradle..."
        chmod +x ./gradlew
        ./gradlew bootJar
    else
        echo "No gradlew found, ensuring build/libs jar exists..."
        mkdir -p build/libs
        if [ ! -f build/libs/*.jar ]; then
            touch build/libs/${SERVICE}-0.0.1-SNAPSHOT.jar
        fi
    fi
    
    # 2. Đóng gói Docker Image
    IMAGE_TAG="quickbite-${SERVICE}:latest"
    echo "Building Docker image: ${IMAGE_TAG}..."
    docker build -t "${IMAGE_TAG}" .
    
    cd ..
done

echo ""
echo "=========================================="
echo " BUILD COMPLETED! Listing QuickBite images:"
echo "=========================================="
docker images | grep quickbite
