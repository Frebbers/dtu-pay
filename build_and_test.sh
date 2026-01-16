#!/bin/bash
set -e
echo "Building project..."
mvn package -DskipTests

echo "Starting containers using docker compose..."
docker compose up -d

echo "Running tests..."
mvn test

echo "Stopping containers..."
docker compose down
