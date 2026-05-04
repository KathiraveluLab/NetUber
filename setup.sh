#!/bin/bash

echo "Setting up NetUber Real-World Environment..."

# 1. Pull the Routing Container Image
echo "Pulling Docker Image: osrg/quagga..."
docker pull osrg/quagga

# 2. Check for ActiveMQ and Start if missing
echo "Checking for ActiveMQ on localhost:61616..."
nc -z localhost 61616
if [ $? -eq 0 ]; then
    echo "ActiveMQ is reachable."
else
    echo "ActiveMQ not found. Starting ActiveMQ container..."
    docker run -d --name netuber-activemq -p 61616:61616 -p 8161:8161 rmohr/activemq
    echo "Waiting for ActiveMQ to initialize..."
    sleep 10
fi

# 3. Clean up any stale VR containers
echo "Cleaning up any existing NetUber containers..."
docker rm -f $(docker ps -a -q --filter "name=VR-") 2>/dev/null

echo "Setup Complete. You can now run ./run_sdi.sh"
