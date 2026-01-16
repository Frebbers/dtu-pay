#!/bin/bash
set -e
echo "Stopping containers..."
docker compose down

echo "Building project..."
mvn package -DskipTests

echo "Starting containers using docker compose..."
docker compose up -d --build
sleep 10
echo "Running tests..."
mvn test

echo "Stopping containers..."
docker compose down