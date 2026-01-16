#!/bin/bash
set -e
echo "Stopping containers..."
docker compose down

echo "Building project..."
mvn package -DskipTests

echo "Starting containers using docker compose..."
docker compose up -d --build

echo "Running tests..."
mvn test

echo "Stopping containers..."
docker compose down
