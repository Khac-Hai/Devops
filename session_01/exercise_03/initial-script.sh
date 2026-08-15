#!/bin/bash

set -e

echo "=== Updating system ==="
sudo apt-get update
sudo apt-get upgrade -y

echo "=== Installing required packages ==="
sudo apt-get install -y openjdk-17-jdk git curl

echo "=== Checking quickbite group ==="
if getent group quickbite > /dev/null; then
    echo "Group quickbite already exists."
else
    echo "Creating group quickbite..."
    sudo groupadd quickbite
fi

echo "=== Checking quickbite user ==="
if id quickbite > /dev/null 2>&1; then
    echo "User quickbite already exists."
else
    sudo useradd -r -g quickbite -s /bin/false quickbite
fi

echo "=== Setup completed ==="
