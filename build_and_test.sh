#!/bin/bash
set -e

echo "Building DTU Pay Server..."
sh dtu-pay-server/build.sh

echo "Running server tests..."
./mvnw -q test -f dtu-pay-server/pom.xml

echo "Starting Quarkus server using docker compose..."
docker compose -f dtu-pay-server/compose.yml up -d

echo "Building DTU Pay Client..."
./mvnw -q compile -f dtu-pay-client/pom.xml

echo "Running client tests..."
./mvnw -q test -f dtu-pay-client/pom.xml

echo "Stopping Quarkus container..."
docker compose -f dtu-pay-server/compose.yml down
