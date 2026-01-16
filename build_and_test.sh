#!/bin/bash
set -e
#TODO update this to run all tests
echo "Building Messaging Utilities..."
mvn clean package -f utilities/messaging-utilities/pom.xml

echo "Building DTU Pay Server..."
#sh dtu-pay-server/build.sh
mvn clean package -f dtu-pay-server/pom.xml
docker compose -f dtu-pay-server/compose.yml build

#echo "Running server tests..."
#mvn -q test -f dtu-pay-server/pom.xml

echo "Starting Quarkus server using docker compose..."
docker compose -f dtu-pay-server/compose.yml up -d

echo "Building DTU Pay Client..."
mvn -q compile -f dtu-pay-E2ETest/pom.xml

echo "Running client tests..."
mvn -q test -f dtu-pay-E2ETest/pom.xml

echo "Stopping Quarkus container..."
docker compose -f dtu-pay-server/compose.yml down
