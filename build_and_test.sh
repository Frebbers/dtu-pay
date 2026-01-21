#!/bin/bash
set -e

echo "Building project..."
mvn package

echo "Starting containers using docker compose..."
docker compose up -d --build
echo "Waiting for services to start..."
sleep 10
echo "Running tests..."
mvn test 
