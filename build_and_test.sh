#!/bin/bash
set -e

echo "Building DTU Pay Server..."
cd dtu-pay-server
chmod +x ../mvnw
../mvnw compile
../mvnw package -Dmaven.test.skip

echo "Starting Quarkus server..."
java -jar target/quarkus-app/quarkus-run.jar &
QUARKUS_PID=$!
sleep 5  # Give server time to start

echo "Running server tests..."
../mvnw -q test

echo "Building DTU Pay Client..."
cd ../dtu-pay-client
../mvnw compile

echo "Running client tests..."
../mvnw -q test

echo "Stopping Quarkus application with PID $QUARKUS_PID"
kill $QUARKUS_PID
cd ..

