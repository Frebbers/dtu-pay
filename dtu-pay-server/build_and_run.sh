#!/bin/bash
set -e

echo "Building DTU Pay Server..."
mvn clean package
docker compose build
# Docker compose
echo "Starting Quarkus server..."
docker compose up -d

