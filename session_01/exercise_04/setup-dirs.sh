#!/bin/bash

set -e

echo "=== Creating QuickBite application directory ==="
sudo mkdir -p /opt/quickbite/user-service

echo "=== Setting ownership ==="
sudo chown -R quickbite:quickbite /opt/quickbite

echo "=== Setting permissions ==="
sudo chmod 750 /opt/quickbite

echo "=== Setup completed ==="

