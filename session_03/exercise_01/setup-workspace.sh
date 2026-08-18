#!/bin/bash

# Create user rikkeilms
sudo useradd -m -s /bin/bash rikkeilms 2>/dev/null || true

# Create workspace directory
sudo mkdir -p /opt/rikkei/course-service

# Change owner and group
sudo chown rikkeilms:rikkeilms /opt/rikkei/course-service

# Set permissions
sudo chmod 755 /opt/rikkei/course-service

# Verify
id rikkeilms
ls -ld /opt/rikkei/course-service
