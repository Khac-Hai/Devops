#!/bin/bash

# Build and package 4 Spring Boot Microservices
echo "Building QuickBite Microservices..."

docker build -t quickbite-user-service:latest ./user-service
docker build -t quickbite-restaurant-service:latest ./restaurant-service
docker build -t quickbite-order-service:latest ./order-service
docker build -t quickbite-notification-service:latest ./notification-service

echo "Checking built images:"
docker images | grep quickbite
